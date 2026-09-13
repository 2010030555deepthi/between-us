package com.betweenus.between_us.service;

import com.betweenus.between_us.model.User;
import com.betweenus.between_us.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String username, String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        String encryptedPassword = passwordEncoder.encode(password);

        User user = new User(username, encryptedPassword);

        return userRepository.save(user);
    }

    public boolean loginUser(String username, String password) {

        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(
                        password,
                        user.getPassword()
                ))
                .orElse(false);
    }
}