package com.project.concert.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingHistoryDTO {
    private String concertName;
    private String concertDate;
    private String paymentStatus;        // PENDING, COMPLETED, EXPIRED
    private LocalDateTime bookedAt;
    private List<String> seatNumbers;
    private BigDecimal totalPrice;

    private boolean canContinuePayment;  // if PENDING & not expired
    private LocalDateTime expiresAt;     // 5 minutes from bookedAt

    private Long concertId;
    private List<Long> seatIds;

    // Getters & setters
    public String getConcertName() { return concertName; }
    public void setConcertName(String concertName) { this.concertName = concertName; }

    public String getConcertDate() { return concertDate; }
    public void setConcertDate(String concertDate) { this.concertDate = concertDate; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public List<String> getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(List<String> seatNumbers) { this.seatNumbers = seatNumbers; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public boolean isCanContinuePayment() { return canContinuePayment; }
    public void setCanContinuePayment(boolean canContinuePayment) { this.canContinuePayment = canContinuePayment; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }

    public List<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(List<Long> seatIds) { this.seatIds = seatIds; }
}