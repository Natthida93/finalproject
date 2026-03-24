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
@CrossOrigin(origins = "*")
public class BookingController {

    private final SeatService seatService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    private final  ConcertRepository concertRepository;


    public BookingController(SeatService seatService,
                             BookingRepository bookingRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository,
                             ConcertRepository concertRepository) {
        this.seatService = seatService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.concertRepository = concertRepository;
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
            for (Seat seat : seats) {
                if (seat.getLockedById() == null ||
                        !seat.getLockedById().equals(payment.getUser().getId())) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not locked for this user");
                }

                if (seat.getStatus() != SeatStatus.LOCKED) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is not locked");
                }

                seat.setStatus(SeatStatus.BOOKED);
                seat.setLockedById(null);
                seat.setLockedUntil(null);
                seatService.saveSeat(seat);
            }

            // === FIX: Explicitly set bookedAt to avoid DB default error ===
            Booking booking = new Booking();
            booking.setUser(payment.getUser());
            booking.setConcert(payment.getConcert());
            booking.setSeats(seats);
            booking.setPayment(payment);
            booking.setBookedAt(LocalDateTime.now()); // <-- explicitly set booking time

            bookingRepository.save(booking);

            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ================= GET BOOKING BY PAYMENT =================
    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<?> getBookingByPayment(@PathVariable Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);
        if (payment == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");

        Booking booking = bookingRepository.findByPayment(payment)
                .orElse(null);
        if (booking == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");

        return ResponseEntity.ok(booking);
    }

    // ================= USER BOOKING HISTORY =================
    @GetMapping("/history/{userEmail}")
    public ResponseEntity<?> getUserBookingHistory(@PathVariable String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        List<Booking> bookings = bookingRepository.findByUser(user);
        List<Map<String, Object>> history = new ArrayList<>();

        for (Booking b : bookings) {
            Map<String, Object> item = new HashMap<>();
            // Include concert ID for frontend
            item.put("concertId", b.getConcert().getId());
            item.put("concertName", b.getConcert().getTitle());
            item.put("concertDate", b.getConcert().getDate());

            // Include payment ID for polling
            item.put("paymentId", b.getPayment().getId());
            item.put("paymentStatus", b.getPayment().getStatus().name());

            item.put("totalPrice", b.getSeats().stream().mapToDouble(Seat::getPrice).sum());

            List<Map<String, Object>> seatsInfo = new ArrayList<>();
            for (Seat s : b.getSeats()) {
                Map<String, Object> seatMap = new HashMap<>();
                seatMap.put("seatNumber", s.getSeatNumber());
                seatMap.put("section", s.getSection().getName());
                seatMap.put("price", s.getPrice());
                seatsInfo.add(seatMap);
            }
            item.put("seats", seatsInfo);
            item.put("bookedAt", b.getBookedAt());

            history.add(item);
        }

        return ResponseEntity.ok(history);
    }

    @PostMapping("/alipay")
    public ResponseEntity<?> payWithAlipay(@RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Long concertId = Long.valueOf(payload.get("concertId").toString());
            Double totalPrice = Double.valueOf(payload.get("price").toString()); // Use your price field

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Concert concert = concertRepository.findById(concertId)
                    .orElseThrow(() -> new RuntimeException("Concert not found"));

            Payment payment = new Payment();
            payment.setUser(user);
            payment.setConcert(concert);
            payment.setPrice(totalPrice);
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            // Generate sandbox QR code
            String qrCodeUrl = "https://via.placeholder.com/200?text=Alipay+Sandbox+QR";

            Map<String, Object> response = new HashMap<>();
            response.put("qrCodeUrl", qrCodeUrl);
            response.put("paymentId", payment.getId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    // 2️⃣ Handle Alipay notify callback
    @PostMapping("/alipay/notify")
    public ResponseEntity<String> handleAlipayNotify(@RequestParam Long paymentId,
                                                     @RequestParam String tradeStatus) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");

        Payment payment = optionalPayment.get();

        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // Optional: automatically confirm booking if you want
            // You can reuse your confirmBooking() logic here if desired

            return ResponseEntity.ok("SUCCESS");
        }

        return ResponseEntity.ok("FAILED");
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