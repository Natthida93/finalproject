package com.project.concert.controller;

import com.project.concert.model.*;
import com.project.concert.repository.*;
import com.project.concert.service.SeatService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final SeatService seatService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public BookingController(SeatService seatService,
                             BookingRepository bookingRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository) {
        this.seatService = seatService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    // ================= LOCK SEATS =================
    @PostMapping("/lock")
    public ResponseEntity<?> lockSeats(@RequestBody BookingRequest request) {
        try {
            User user = userRepository.findByEmail(request.getUserEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid user"));

            List<Seat> lockedSeats = new ArrayList<>();
            for (Long seatId : request.getSeatIds()) {
                Seat seat = seatService.getSeatById(seatId);

                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not available");
                }

                seat.setStatus(SeatStatus.LOCKED);
                seat.setLockedById(user.getId());
                seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
                seatService.saveSeat(seat);
                lockedSeats.add(seat);
            }

            return ResponseEntity.ok("Seats locked successfully for 5 minutes.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ================= CONFIRM BOOKING AFTER PAYMENT =================
    @Transactional
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmBooking(@RequestBody PaymentConfirmationRequest request) {
        try {
            Payment payment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));


            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                throw new RuntimeException("Payment not completed yet");
            }

            Set<Seat> seats = payment.getSeats();
            List<Booking> bookings = new ArrayList<>();
            for (Seat seat : seats) {
                if (seat.getStatus() != SeatStatus.LOCKED || !seat.getLockedById().equals(payment.getUser().getId())) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not properly locked for this user");
                }

                // Mark seat as booked
                seat.setStatus(SeatStatus.BOOKED);
                seat.setLockedById(null);
                seat.setLockedUntil(null);
                seatService.saveSeat(seat);

                // Create booking record
                Booking booking = new Booking();
                booking.setUser(payment.getUser());
                booking.setConcert(seat.getConcert());
                booking.setSeatId(seat.getId());
                booking.setSeatNumber(seat.getSeatNumber());
                booking.setZoneName(seat.getSection().getName());
                booking.setTotalPrice(seat.getPrice() != null ? seat.getPrice() : 0);
                booking.setStatus("CONFIRMED");
                booking.setDeliveryMethod(payment.getDeliveryMethod());
                bookingRepository.save(booking);
                bookings.add(booking);
            }

            return ResponseEntity.ok(bookings);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    // ================= USER BOOKING HISTORY =================
    @GetMapping("/history/{userEmail}")
    public ResponseEntity<?> getUserBookingHistory(@PathVariable String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        if(user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        List<Booking> bookings = bookingRepository.findByUser(user);
        List<Map<String,Object>> history = new ArrayList<>();

        for(Booking b : bookings){
            Map<String,Object> item = new HashMap<>();
            item.put("concertName", b.getConcert().getTitle());
            item.put("concertDate", b.getConcert().getDate());
            item.put("seatNumber", b.getSeatNumber());
            item.put("zoneName", b.getZoneName());
            item.put("totalPrice", b.getTotalPrice());
            item.put("status", b.getStatus());
            item.put("deliveryMethod", b.getDeliveryMethod());
            history.add(item);
        }

        return ResponseEntity.ok(history);
    }

    // ================= DTOs =================
    public static class BookingRequest {
        private String userEmail;
        private Set<Long> seatIds;

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public Set<Long> getSeatIds() { return seatIds; }
        public void setSeatIds(Set<Long> seatIds) { this.seatIds = seatIds; }
    }

    public static class PaymentConfirmationRequest {
        private Long paymentId;

        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    }
}