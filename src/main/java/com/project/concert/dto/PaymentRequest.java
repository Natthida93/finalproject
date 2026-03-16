package com.project.concert.dto;

import java.util.Set;

public class PaymentRequest {
    private Long userId;
    private Long concertId;
    private Set<Long> seatIds; // frontend sends as JSON array -> deserializes as Set
    private String deliveryMethod;

    // ===== GETTERS / SETTERS =====
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }

    public Set<Long> getSeatIds() { return seatIds; }
    public void setSeatIds(Set<Long> seatIds) { this.seatIds = seatIds; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
}