package com.project.concert.controller;

import com.project.concert.model.*;
import com.project.concert.repository.*;
import com.project.concert.service.PaymentService;
import com.project.concert.service.SeatService;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final SeatService seatService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ConcertRepository concertRepository;
    private final PaymentService paymentService;

    public BookingController(SeatService seatService,
                             BookingRepository bookingRepository,
                             UserRepository userRepository,
                             PaymentRepository paymentRepository,
                             ConcertRepository concertRepository,
                             PaymentService paymentService) {
        this.seatService = seatService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.concertRepository = concertRepository;
        this.paymentService = paymentService;
    }

    // ================= GET BOOKING BY PAYMENT =================
    @Transactional(readOnly = true)
    @GetMapping("/by-payment/{paymentId}")
    public ResponseEntity<?> getBookingByPayment(@PathVariable Long paymentId) {

        Optional<Booking> bookingOpt =
                bookingRepository.findByPaymentIdWithSeatsAndConcert(paymentId);

        if (bookingOpt.isPresent()) {
            return ResponseEntity.ok(new BookingDTO(bookingOpt.get()));
        }

        Optional<Payment> paymentOpt =
                paymentRepository.findByIdWithConcertAndSeats(paymentId);

        if (paymentOpt.isPresent()) {
            return ResponseEntity.ok(new BookingDTO(paymentOpt.get()));
        }

        return ResponseEntity.notFound().build();
    }

    // ==================== DTOs ====================

    public static class BookingDTO {
        private Long bookingId;
        private LocalDateTime bookedAt;
        private ConcertDTO concert;
        private Set<SeatDTO> seats;
        private PaymentDTO payment;
        private String deliveryMethod; // ✅ NEW FIELD

        public BookingDTO(Booking b) {
            this.bookingId = b.getId();
            this.bookedAt = b.getBookedAt();
            this.concert = new ConcertDTO(b.getConcert());
            this.seats = b.getSeats()
                    .stream()
                    .map(SeatDTO::new)
                    .collect(Collectors.toSet());

            this.payment = b.getPayment() != null
                    ? new PaymentDTO(b.getPayment())
                    : null;

            // IMPORTANT: get from Booking
            this.deliveryMethod = b.getDeliveryMethod();
        }

        public BookingDTO(Payment p) {
            this.bookingId = null;
            this.bookedAt = p.getCompletedAt();
            this.concert = new ConcertDTO(p.getConcert());
            this.seats = p.getSeats()
                    .stream()
                    .map(SeatDTO::new)
                    .collect(Collectors.toSet());

            this.payment = new PaymentDTO(p);

            // FIX: Payment does NOT have deliveryMethod
            this.deliveryMethod = null;
        }

        public Long getBookingId() { return bookingId; }
        public LocalDateTime getBookedAt() { return bookedAt; }
        public ConcertDTO getConcert() { return concert; }
        public Set<SeatDTO> getSeats() { return seats; }
        public PaymentDTO getPayment() { return payment; }
        public String getDeliveryMethod() { return deliveryMethod; } // ✅
    }

    // ==================== Seat DTO ====================
    public static class SeatDTO {
        private Long seatId;
        private String label;
        private String status;
        private BigDecimal price;

        public SeatDTO(Seat s) {
            this.seatId = s.getId();
            this.label = s.getSeatNumber();
            this.status = s.getStatus().name();
            this.price = s.getPrice();
        }

        public Long getSeatId() { return seatId; }
        public String getLabel() { return label; }
        public String getStatus() { return status; }
        public BigDecimal getPrice() { return price; }
    }

    // ==================== Concert DTO ====================
    public static class ConcertDTO {
        private Long concertId;
        private String title;
        private String startTime;

        public ConcertDTO(Concert c) {
            this.concertId = c.getId();
            this.title = c.getTitle();
            this.startTime = c.getStartTime() != null
                    ? c.getStartTime().toString()
                    : "TBD";
        }

        public Long getConcertId() { return concertId; }
        public String getTitle() { return title; }
        public String getStartTime() { return startTime; }
    }

    // ==================== Payment DTO ====================
    public static class PaymentDTO {
        private Long paymentId;
        private String status;
        private BigDecimal price;
        private String outTradeNo;

        public PaymentDTO(Payment p) {
            this.paymentId = p.getId();
            this.status = p.getStatus().name();
            this.price = p.getPrice();
            this.outTradeNo = p.getOutTradeNo();
        }

        public Long getPaymentId() { return paymentId; }
        public String getStatus() { return status; }
        public BigDecimal getPrice() { return price; }
        public String getOutTradeNo() { return outTradeNo; }
    }
}