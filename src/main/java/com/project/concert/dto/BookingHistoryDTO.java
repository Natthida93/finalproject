package com.project.concert.dto;


import java.time.LocalDateTime;
import java.util.List;

public class BookingHistoryDTO {
    private String concertName;
    private String concertDate;
    private String paymentStatus;
    private LocalDateTime bookedAt;
    private List<String> seatNumbers;
    private double totalPrice;

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
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}