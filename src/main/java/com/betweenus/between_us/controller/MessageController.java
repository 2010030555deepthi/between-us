package com.betweenus.between_us.controller;

import com.betweenus.between_us.model.Message;
import com.betweenus.between_us.repository.MessageRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public List<Message> getMessages(
            @RequestParam(required = false) String search
    ) {
        if (search != null && !search.trim().isEmpty()) {
            return messageRepository
                    .findByTextContainingIgnoreCaseOrderByCreatedAtAsc(
                            search.trim()
                    );
        }

        return messageRepository.findAllByOrderByCreatedAtAsc();
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody Message request,
            HttpSession session
    ) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        if (request.getText() == null ||
                request.getText().trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Message cannot be empty");
        }

        Message message = new Message(
                username,
                request.getText().trim()
        );

        message.setReplyToId(request.getReplyToId());
        message.setReplyToText(request.getReplyToText());

        return ResponseEntity.ok(
                messageRepository.save(message)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editMessage(
            @PathVariable Long id,
            @RequestBody Message request,
            HttpSession session
    ) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        Message message = messageRepository
                .findById(id)
                .orElse(null);

        if (message == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (!message.getSender().equals(username)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You can edit only your own messages");
        }

        message.setText(request.getText().trim());
        message.setEdited(true);

        return ResponseEntity.ok(
                messageRepository.save(message)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id,
            HttpSession session
    ) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        Message message = messageRepository
                .findById(id)
                .orElse(null);

        if (message == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (!message.getSender().equals(username)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You can delete only your own messages");
        }

        messageRepository.delete(message);

        return ResponseEntity.ok("Message deleted");
    }

    @PatchMapping("/{id}/reaction")
    public ResponseEntity<?> reactToMessage(
            @PathVariable Long id,
            @RequestBody Message request,
            HttpSession session
    ) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in first");
        }

        Message message = messageRepository
                .findById(id)
                .orElse(null);

        if (message == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        message.setReaction(request.getReaction());

        return ResponseEntity.ok(
                messageRepository.save(message)
        );
    }
}