package com.betweenus.between_us.controller;

import com.betweenus.between_us.model.Media;
import com.betweenus.between_us.repository.MediaRepository;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@CrossOrigin
public class MediaController {

    private final MediaRepository mediaRepository;

    private final Path uploadDirectory =
            Paths.get("uploads");

    public MediaController(
            MediaRepository mediaRepository
    ) {
        this.mediaRepository = mediaRepository;

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException error) {
            throw new RuntimeException(
                    "Could not create uploads folder",
                    error
            );
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Please select a file");
        }

        String contentType =
                file.getContentType();

        if (
                contentType == null ||
                !(
                        contentType.startsWith("image/") ||
                        contentType.startsWith("video/") ||
                        contentType.startsWith("audio/")
                )
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Only images, videos and audio files are allowed"
                    );
        }

        try {

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null) {
                originalFileName = "media-file";
            }

            String extension = "";

            int lastDot =
                    originalFileName.lastIndexOf(".");

            if (lastDot >= 0) {
                extension =
                        originalFileName.substring(lastDot);
            }

            String storedFileName =
                    UUID.randomUUID() + extension;

            Path targetPath =
                    uploadDirectory.resolve(storedFileName);

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Media media = new Media(
                    originalFileName,
                    storedFileName,
                    contentType,
                    "/uploads/" + storedFileName,
                    username
            );

            Media savedMedia =
                    mediaRepository.save(media);

            return ResponseEntity.ok(savedMedia);

        } catch (IOException error) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllMedia(
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        List<Media> mediaList =
                mediaRepository
                        .findAllByOrderByCreatedAtDesc();

        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/saved")
    public ResponseEntity<?> getSavedMedia(
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        return ResponseEntity.ok(
                mediaRepository
                        .findBySavedTrueOrderByCreatedAtDesc()
        );
    }

    @PatchMapping("/{id}/save")
    public ResponseEntity<?> toggleSaved(
            @PathVariable Long id,
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        Media media =
                mediaRepository
                        .findById(id)
                        .orElse(null);

        if (media == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        media.setSaved(!media.isSaved());

        return ResponseEntity.ok(
                mediaRepository.save(media)
        );
    }

   @DeleteMapping("/{id}")
public ResponseEntity<?> deleteMedia(
        @PathVariable Long id,
        HttpSession session
) {

    String username =
            (String) session.getAttribute("username");

    if (username == null) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Please log in first");
    }

    Media media =
            mediaRepository
                    .findById(id)
                    .orElse(null);

    if (media == null) {
        return ResponseEntity
                .notFound()
                .build();
    }

    if (!media.getUploadedBy().equals(username)) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        "You can delete only your own media"
                );
    }

    try {

        Path filePath =
                uploadDirectory.resolve(
                        media.getStoredFileName()
                );

        Files.deleteIfExists(filePath);

    } catch (IOException error) {

        System.out.println(
                "Could not delete physical file: "
                        + error.getMessage()
        );
    }

    mediaRepository.delete(media);

    return ResponseEntity.ok(
            "Media deleted successfully"
    );
}
}