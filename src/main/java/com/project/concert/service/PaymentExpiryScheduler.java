package com.project.concert.service;

import com.project.concert.model.Payment;
import com.project.concert.model.PaymentStatus;
import com.project.concert.model.Seat;
import com.project.concert.model.SeatStatus;
import com.project.concert.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentExpiryScheduler {

    private final PaymentRepository paymentRepository;

    public PaymentExpiryScheduler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    @Transactional  // Important! Allows lazy loading of seats
    public void expirePendingPayments() {
        LocalDateTime now = LocalDateTime.now();

        // Fetch all PENDING payments
        List<Payment> pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING);

        for (Payment payment : pendingPayments) {
            // Check if 5 minutes have passed since creation
            if (payment.getCreatedAt().plusMinutes(5).isBefore(now)) {
                // Unlock all seats associated with this payment
                for (Seat seat : payment.getSeats()) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setLockedById(null);
                    seat.setLockedUntil(null);
                }

                // Mark payment as EXPIRED
                payment.setStatus(PaymentStatus.EXPIRED);

                // Save updated payment and seats (cascade if configured)
                paymentRepository.save(payment);

                System.out.println("Expired payment ID: " + payment.getId() + " and unlocked seats.");
            }
        }
    }
}