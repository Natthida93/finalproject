package com.project.concert.controller;

import com.project.concert.dto.SeatDto;
import com.project.concert.model.*;
import com.project.concert.repository.ConcertRepository;
import com.project.concert.repository.PaymentRepository;
import com.project.concert.repository.SeatRepository;
import com.project.concert.service.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:5174")
public class SeatController {

    private final SeatService seatService;
    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;

    public SeatController(SeatService seatService,
                          ConcertRepository concertRepository,
                          SeatRepository seatRepository,
                          PaymentRepository paymentRepository) {
        this.seatService = seatService;
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
    }

    // ================= LOCK SEAT =================
    @PostMapping("/lock")
    public ResponseEntity<?> lockSeat(@RequestParam Long seatId, @RequestParam Long userId) {
        try {
            Seat seat = seatService.lockSeat(seatId, userId);
            return ResponseEntity.ok(new SeatDto(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seat.getStatus().name(),
                    seat.getPrice() != null ? seat.getPrice() : BigDecimal.ZERO
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ================= UNLOCK SEAT =================
    @PostMapping("/unlock")
    public ResponseEntity<?> unlockSeat(@RequestParam Long seatId, @RequestParam Long userId) {
        try {
            seatService.unlockSeat(seatId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ================= CONFIRM SEAT =================
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmSeat(@RequestParam Long seatId, @RequestParam Long userId) {
        try {
            Seat seat = seatService.confirmBooking(seatId, userId);

            if (seat.getSeatNumber() == null) throw new RuntimeException("Seat number missing");

            User user = seatService.getUserById(userId);

            Payment payment = new Payment();
            payment.setConcert(seat.getConcert());
            payment.setUser(user);
            payment.setSeats(Set.of(seat));
            payment.setPrice(seat.getPrice() != null ? seat.getPrice() : BigDecimal.ZERO);
            payment.setDeliveryMethod("CONCERT");
            payment.setShippingAddress(null);
            payment.setUserIdNumber(null);

            paymentRepository.save(payment);

            return ResponseEntity.ok(new SeatDto(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seat.getStatus().name(),
                    seat.getPrice() != null ? seat.getPrice() : BigDecimal.ZERO
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // ================= VALIDATE LOCKS =================
    @PostMapping("/validate-locks")
    public ResponseEntity<?> validateLocks(@RequestBody Map<String, Object> payload) {
        try {
            List<Integer> seatIds = (List<Integer>) payload.get("seatIds");
            String userEmail = (String) payload.get("userEmail");

            List<Seat> availableSeats = seatService.validateSeatLocks(seatIds, userEmail);

            return ResponseEntity.ok(availableSeats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // ================= GET SEATS BY ZONE =================
    @GetMapping("/concert/{concertId}/zone")
    public ResponseEntity<List<SeatDto>> getSeatMapByZone(@PathVariable Long concertId,
                                                          @RequestParam String zoneName) {
        try {
            List<Seat> seats = seatService.getSeatsByConcert(concertId);
            List<SeatDto> seatGrid = seats.stream()
                    .filter(s -> s.getSection() != null && zoneName.equals(s.getSection().getName()))
                    .map(s -> new SeatDto(
                            s.getId(),
                            s.getSeatNumber(),
                            s.getStatus() != null ? s.getStatus().name() : SeatStatus.AVAILABLE.name(),
                            s.getPrice() != null ? s.getPrice() : BigDecimal.ZERO
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(seatGrid);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // ================= RELEASE EXPIRED LOCKS =================
    @PostMapping("/release-expired")
    public ResponseEntity<Integer> releaseExpiredLocks() {
        try {
            int released = seatService.releaseExpiredLocks(LocalDateTime.now());
            return ResponseEntity.ok(released);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }
}