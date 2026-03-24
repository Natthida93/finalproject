package com.project.concert.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentRequest{
    private Long paymentId;
    private String formHtml; // nullable if pending
    private List<SeatDTO> seats;
    private String deliveryMethod;
    private Double totalPrice;
    private LocalDateTime lockedUntil;

    // ===== Getters / Setters =====
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getFormHtml() { return formHtml; }
    public void setFormHtml(String formHtml) { this.formHtml = formHtml; }

    public List<SeatDTO> getSeats() { return seats; }
    public void setSeats(List<SeatDTO> seats) { this.seats = seats; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    // ===== Nested SeatDTO =====
    public static class SeatDTO {
        private Long id;
        private String seatNumber;
        private LocalDateTime lockedUntil;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    }
}