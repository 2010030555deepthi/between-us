package com.betweenus.between_us.controller;

import com.betweenus.between_us.model.User;
import com.betweenus.between_us.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        try {
            userService.registerUser(
                    user.getUsername(),
                    user.getPassword()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("User registered successfully");

        } catch (RuntimeException error) {
            return ResponseEntity
                    .badRequest()
                    .body(error.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user,
            HttpSession session
    ) {

        boolean valid = userService.loginUser(
                user.getUsername(),
                user.getPassword()
        );

        if (valid) {

            session.setAttribute(
                    "username",
                    user.getUsername()
            );

            session.setMaxInactiveInterval(
                    30 * 60
            );

            return ResponseEntity.ok(
                    "Login successful"
            );
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid username or password");
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> currentUser(
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute(
                        "username"
                );

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Not logged in");
        }

        return ResponseEntity.ok(username);
    }

    @GetMapping("/session-check")
    public ResponseEntity<?> sessionCheck(
            HttpSession session
    ) {

        String username =
                (String) session.getAttribute(
                        "username"
                );

        if (username == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Session expired");
        }

        return ResponseEntity.ok(
                "Session active for " + username
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpSession session
    ) {

        session.invalidate();

        return ResponseEntity.ok(
                "Logged out"
        );
    }
}