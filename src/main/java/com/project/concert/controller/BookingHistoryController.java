package com.project.concert.controller;

import com.project.concert.dto.BookingHistoryDTO;
import com.project.concert.model.Payment;
import com.project.concert.model.PaymentStatus;
import com.project.concert.model.Seat;
import com.project.concert.repository.PaymentRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/bookings")
@Transactional(readOnly = true)
@CrossOrigin(
        origins = "https://concertticketingsystem.netlify.app",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class BookingHistoryController {

    private final PaymentRepository paymentRepository;

    public BookingHistoryController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/history/{email}")
    public List<BookingHistoryDTO> getBookingHistory(@PathVariable String email) {

        List<Payment> payments = paymentRepository.findByUserEmail(email);

        return payments.stream().map(payment -> {

            BookingHistoryDTO dto = new BookingHistoryDTO();

            // Concert info
            dto.setConcertName(payment.getConcert().getTitle());
            dto.setConcertDate(payment.getConcert().getDate().toString());
            dto.setConcertId(payment.getConcert().getId());

            // Payment info
            dto.setPaymentStatus(payment.getStatus().name());
            dto.setBookedAt(payment.getCreatedAt());
            dto.setTotalPrice(payment.getPrice());  // BigDecimal

            // Seat info
            dto.setSeatNumbers(
                    payment.getSeats()
                            .stream()
                            .map(Seat::getSeatNumber)
                            .collect(Collectors.toList())
            );
            dto.setSeatIds(
                    payment.getSeats()
                            .stream()
                            .map(Seat::getId)
                            .collect(Collectors.toList())
            );

            // Pending payment info
            if (payment.getStatus() == PaymentStatus.PENDING) {
                LocalDateTime expiresAt = payment.getCreatedAt().plusMinutes(5);
                dto.setExpiresAt(expiresAt);
                dto.setCanContinuePayment(LocalDateTime.now().isBefore(expiresAt));
            } else {
                dto.setExpiresAt(null);
                dto.setCanContinuePayment(false);
            }

            return dto;

        }).collect(Collectors.toList());
    }
}