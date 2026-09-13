package com.betweenus.between_us.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/typing")
@CrossOrigin
public class TypingController {

    private final Map<String, Long> typingUsers =
            new ConcurrentHashMap<>();

    private final Map<String, Long> onlineUsers =
            new ConcurrentHashMap<>();

    @PostMapping
    public String updateStatus(
            @RequestBody Map<String, Boolean> request,
            HttpSession session
    ) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return "Please log in first";
        }

        onlineUsers.put(username, System.currentTimeMillis());

        Boolean typing = request.get("typing");

        if (Boolean.TRUE.equals(typing)) {
            typingUsers.put(username, System.currentTimeMillis());
        } else {
            typingUsers.remove(username);
        }

        cleanOldUsers();

        return "Status updated";
    }

    @GetMapping
    public Map<String, Object> getStatus(HttpSession session) {
        String currentUser =
                (String) session.getAttribute("username");

        cleanOldUsers();

        String typingUser = null;

        for (String username : typingUsers.keySet()) {
            if (!username.equals(currentUser)) {
                typingUser = username;
                break;
            }
        }

        String onlineUser = null;

        for (String username : onlineUsers.keySet()) {
            if (!username.equals(currentUser)) {
                onlineUser = username;
                break;
            }
        }

        return Map.of(
                "typingUser",
                typingUser == null ? "" : typingUser,

                "onlineUser",
                onlineUser == null ? "" : onlineUser
        );
    }

    private void cleanOldUsers() {
        long now = System.currentTimeMillis();

        typingUsers.entrySet().removeIf(entry ->
                now - entry.getValue() > 4000
        );

        onlineUsers.entrySet().removeIf(entry ->
                now - entry.getValue() > 10000
        );
    }
}