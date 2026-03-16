package com.project.concert.service;

import com.project.concert.dto.LoginRequest;
import com.project.concert.dto.RegisterRequest;
import com.project.concert.model.Role;
import com.project.concert.model.User;
import com.project.concert.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //register
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists!";
        }

        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            return "ID/Passport number already registered!";
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIdNumber(request.getIdNumber());
        user.setAddress(request.getAddress());
        user.setRole(Role.USER);

        userRepository.save(user);

        return "User registered successfully!";
    }

    //login
    public boolean login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .map(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .orElse(false);
    }
}