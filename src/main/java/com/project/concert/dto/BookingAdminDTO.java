package com.project.concert.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class BookingAdminDTO {

    public Long id;
    public String userEmail;
    public String concertTitle;
    public List<String> seats;
    public BigDecimal totalPrice;   // <-- changed from double to BigDecimal
    public String paymentStatus;
    public LocalDateTime bookedAt;

    public BookingAdminDTO(Long id,
                           String userEmail,
                           String concertTitle,
                           List<String> seats,
                           BigDecimal totalPrice,
                           String paymentStatus,
                           LocalDateTime bookedAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.concertTitle = concertTitle;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.bookedAt = bookedAt;
    }
}