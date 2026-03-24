package com.project.concert.controller;

import com.project.concert.model.Booking;
import com.project.concert.model.Payment;
import com.project.concert.model.User;
import com.project.concert.repository.BookingRepository;
import com.project.concert.repository.PaymentRepository;
import com.project.concert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // ================== ADMIN LOGIN ==================
    @PostMapping("/login")
    public ResponseEntity<String> adminLogin(@RequestBody User admin) {
        String email = admin.getEmail();
        String password = admin.getPassword();

        if ("janeadmin@gmail.com".equals(email) && "admin123".equals(password)) {
            return ResponseEntity.ok("ADMIN_LOGIN_SUCCESS");
        }
        return ResponseEntity.status(401).body("INVALID_ADMIN");
    }

    // ================== VIEW ALL USERS ==================
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ================== VIEW ALL BOOKINGS ==================
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    // ================== VIEW ALL PAYMENTS ==================
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    // ================== DELETE USER ==================
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // ================== DELETE BOOKING ==================
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    // ================== DELETE PAYMENT ==================
    @DeleteMapping("/payments/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id) {
        if (!paymentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        paymentRepository.deleteById(id);
        return ResponseEntity.ok("Payment deleted successfully");
    }
}