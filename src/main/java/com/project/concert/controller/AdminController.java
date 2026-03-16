package com.project.concert.controller;

import com.project.concert.model.Booking;
import com.project.concert.model.Payment;
import com.project.concert.model.User;
import com.project.concert.repository.BookingRepository;
import com.project.concert.repository.PaymentRepository;
import com.project.concert.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    @PostMapping("/login")
    public String adminLogin(@RequestBody User admin) {
        String email = admin.getEmail();
        String password = admin.getPassword();


        if ("janeadmin@gmail.com".equals(email) && "admin123".equals(password)) {
            return "ADMIN_LOGIN_SUCCESS";
        }
        return "INVALID_ADMIN";
    }

    // --- View all users ---
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- View all bookings ---
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // --- View all payments ---
    @GetMapping("/payments")
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // --- Delete a user ---
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    // --- Delete a booking ---
    @DeleteMapping("/bookings/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingRepository.deleteById(id);
    }

    // --- Delete a payment ---
    @DeleteMapping("/payments/{id}")
    public void deletePayment(@PathVariable Long id) {
        paymentRepository.deleteById(id);
    }
}
