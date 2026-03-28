package com.project.concert.controller;

import com.project.concert.dto.BookingAdminDTO;
import com.project.concert.model.Booking;
import com.project.concert.model.Payment;
import com.project.concert.model.PaymentStatus;
import com.project.concert.model.User;
import com.project.concert.repository.BookingRepository;
import com.project.concert.repository.PaymentRepository;
import com.project.concert.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

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
        if ("janeadmin@gmail.com".equals(admin.getEmail()) &&
                "admin123".equals(admin.getPassword())) {
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
    @Transactional
    public ResponseEntity<List<BookingAdminDTO>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingAdminDTO> result = new ArrayList<>();

        for (Booking b : bookings) {
            List<String> seatNumbers = new ArrayList<>();
            BigDecimal totalPrice = BigDecimal.ZERO;

            if (b.getSeats() != null) {
                for (var seat : b.getSeats()) {
                    seatNumbers.add(seat.getSeatNumber());
                    if (seat.getPrice() != null) {
                        totalPrice = totalPrice.add(seat.getPrice());
                    }
                }
            }

            result.add(new BookingAdminDTO(
                    b.getId(),
                    b.getUser() != null ? b.getUser().getEmail() : "N/A",
                    b.getConcert() != null ? b.getConcert().getTitle() : "N/A",
                    seatNumbers,
                    totalPrice,  // now passes BigDecimal directly
                    b.getPayment() != null ? b.getPayment().getStatus().name() : "N/A",
                    b.getBookedAt()
            ));
        }

        return ResponseEntity.ok(result);
    }

    // ================== VIEW ALL PAYMENTS ==================
    @GetMapping("/payments")
    public ResponseEntity<List<Map<String, Object>>> getAllPayments() {
        List<Payment> payments = paymentRepository.findAllWithUserAndConcert();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Payment p : payments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("price", p.getPrice());
            item.put("status", p.getStatus() != null ? p.getStatus().name() : "N/A");
            item.put("deliveryMethod", p.getDeliveryMethod());
            item.put("createdAt", p.getCreatedAt());
            item.put("userName", p.getUser() != null ? p.getUser().getFullName() : "N/A");
            item.put("userEmail", p.getUser() != null ? p.getUser().getEmail() : "N/A");
            item.put("concertTitle", p.getConcert() != null ? p.getConcert().getTitle() : "N/A");
            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    // ================== DELETE OPERATIONS ==================
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) return ResponseEntity.notFound().build();
        bookingRepository.deleteById(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id) {
        if (!paymentRepository.existsById(id)) return ResponseEntity.notFound().build();
        paymentRepository.deleteById(id);
        return ResponseEntity.ok("Payment deleted successfully");
    }

    // ================== STATS ==================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();
        long totalPayments = paymentRepository.count();

        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && p.getPrice() != null)
                .map(Payment::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("users", totalUsers);
        stats.put("bookings", totalBookings);
        stats.put("payments", totalPayments);
        stats.put("revenue", totalRevenue);

        return ResponseEntity.ok(stats);
    }

    // ================== ANALYTICS ==================
    @Transactional
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        List<Payment> payments = paymentRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();

        // Revenue by date
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        for (Payment p : payments) {
            if (p.getStatus() == PaymentStatus.COMPLETED && p.getPrice() != null && p.getCreatedAt() != null) {
                String date = p.getCreatedAt().toLocalDate().toString();
                revenueMap.put(date, revenueMap.getOrDefault(date, BigDecimal.ZERO).add(p.getPrice()));
            }
        }

        List<Map<String, Object>> revenue = new ArrayList<>();
        for (String date : revenueMap.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("amount", revenueMap.get(date));
            revenue.add(item);
        }

        // Bookings per concert
        Map<String, Integer> bookingMap = new HashMap<>();
        for (Booking b : bookings) {
            if (b.getConcert() != null) {
                String title = b.getConcert().getTitle();
                bookingMap.put(title, bookingMap.getOrDefault(title, 0) + 1);
            }
        }

        List<Map<String, Object>> bookingStats = new ArrayList<>();
        for (String concert : bookingMap.keySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("concert", concert);
            item.put("count", bookingMap.get(concert));
            bookingStats.add(item);
        }

        // Payment status
        long completed = payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long pending = payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long failed = payments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();

        Map<String, Object> paymentStats = new HashMap<>();
        paymentStats.put("completed", completed);
        paymentStats.put("pending", pending);
        paymentStats.put("failed", failed);

        // Final response
        Map<String, Object> response = new HashMap<>();
        response.put("revenue", revenue);
        response.put("bookings", bookingStats);
        response.put("payments", paymentStats);

        return ResponseEntity.ok(response);
    }
}