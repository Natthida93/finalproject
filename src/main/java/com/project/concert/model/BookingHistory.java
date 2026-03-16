package com.project.concert.model;

import java.time.LocalDateTime;

public class BookingHistory {

    private String concertTitle;
    private String seatNumber;
    private String status;
    private LocalDateTime bookingTime; // map to Booking.createdAt
    private Double totalPrice;

    public BookingHistory(Booking booking) {
        this.concertTitle = booking.getConcert().getTitle();
        this.seatNumber = booking.getSeatNumber();
        this.status = booking.getStatus();
        this.bookingTime = booking.getCreatedAt(); // updated field
        this.totalPrice = booking.getTotalPrice();
    }

    // ===== GETTERS =====
    public String getConcertTitle() { return concertTitle; }
    public String getSeatNumber() { return seatNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public Double getTotalPrice() { return totalPrice; }
}