package com.project.concert.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "seat_number")
    private String seatNumber;

    @Column(name = "zone_name")
    private String zoneName;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private String status;

    @Column(name = "delivery_method", nullable = false)
    private String deliveryMethod; // "CONCERT" or "SHIPPED"

    @Column(name = "user_id_number")
    private String userIdNumber; // Only required if SHIPPED

    @Column(name = "shipping_address")
    private String shippingAddress; // Only required if SHIPPED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // ================= GETTERS / SETTERS =================
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Concert getConcert() { return concert; }
    public Long getSeatId() { return seatId; }
    public String getSeatNumber() { return seatNumber; }
    public String getZoneName() { return zoneName; }
    public Double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public String getUserIdNumber() { return userIdNumber; }
    public String getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setConcert(Concert concert) { this.concert = concert; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    public void setUserIdNumber(String userIdNumber) { this.userIdNumber = userIdNumber; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}