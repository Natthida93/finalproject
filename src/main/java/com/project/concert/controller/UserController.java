package com.project.concert.controller;

import com.project.concert.model.User;
import com.project.concert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(
        origins = "https://concertticketingsystem.netlify.app",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ------------------- REGISTER -------------------
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email is already registered.");
            }

            if (userRepository.existsByIdNumber(user.getIdNumber())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("ID/Passport number already registered.");
            }

            // Hash the password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during registration: " + e.getMessage());
        }
    }

    // ------------------- LOGIN -------------------
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            Optional<User> existingUser = userRepository.findByEmail(loginRequest.getEmail());
            if (existingUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "ERROR", "message", "Email not found"));
            }

            User user = existingUser.get();

            // Use BCrypt to check password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", "ERROR", "message", "Incorrect password"));
            }

            // Return user info and ID for frontend
            return ResponseEntity.ok(Map.of(
                    "status", "USER_LOGIN_SUCCESS",
                    "user", Map.of(
                            "id", user.getId(),
                            "fullName", user.getFullName(),
                            "email", user.getEmail(),
                            "address", user.getAddress(),
                            "idNumber", user.getIdNumber()
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Error during login: " + e.getMessage()));
        }
    }

    // ------------------- GET USER INFO BY EMAIL -------------------
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestParam(required = true) String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email parameter is missing"));
        }

        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "address", user.getAddress(),
                        "idNumber", user.getIdNumber()
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found")));
    }

    // ------------------- GET ALL USERS -------------------
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ------------------- GET USER BY ID -------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    // ------------------- UPDATE USER -------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = existingUser.get();
        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        // Hash the password if it is updated
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.setAddress(updatedUser.getAddress());
        user.setIdNumber(updatedUser.getIdNumber());

        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
    }

    // ------------------- DELETE USER -------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }


    //-----------------UpdateAddress----------------------
    @PutMapping("/update-address")
    public ResponseEntity<?> updateAddress(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newAddress = payload.get("address");

        if(email == null || newAddress == null) {
            return ResponseEntity.badRequest().body("Email or address missing");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();
        user.setAddress(newAddress);
        userRepository.save(user);

        return ResponseEntity.ok("Address updated successfully");
    }
}