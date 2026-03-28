package com.project.concert.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_seats",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private Set<Seat> seats = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    // ✅ NEW FIELD
    @Column(name = "delivery_method")
    private String deliveryMethod;

    @PrePersist
    protected void onCreate() {
        if (bookedAt == null) {
            bookedAt = LocalDateTime.now();
        }
    }

    // ================= GETTERS =================

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Concert getConcert() { return concert; }
    public Set<Seat> getSeats() { return seats; }
    public Payment getPayment() { return payment; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public String getDeliveryMethod() { return deliveryMethod; } // ✅

    // ================= SETTERS =================

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setConcert(Concert concert) { this.concert = concert; }
    public void setSeats(Set<Seat> seats) { this.seats = seats; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
}