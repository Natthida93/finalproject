package com.project.concert.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class BookingAdminDTO {

    public Long id;
    public String userEmail;
    public String concertTitle;
    public List<String> seats;
    public BigDecimal totalPrice;
    public String paymentStatus;
    public Long paymentId;
    public LocalDateTime bookedAt;

    public BookingAdminDTO(Long id,
                           String userEmail,
                           String concertTitle,
                           List<String> seats,
                           BigDecimal totalPrice,
                           String paymentStatus,
                           Long aLong, LocalDateTime bookedAt) {
        this.id = id;
        this.userEmail = userEmail;
        this.concertTitle = concertTitle;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.paymentId = paymentId;
        this.bookedAt = bookedAt;
    }
}