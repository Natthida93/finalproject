package com.project.concert.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "payment")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- MONEY ----------------
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // ---------------- ALIPAY TRADE NO ----------------
    @Column(unique = true, nullable = false)
    private String outTradeNo;

    // ---------------- RELATIONS ----------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    @JsonIgnore
    private Concert concert;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "payment_seats",
            joinColumns = @JoinColumn(name = "payment_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    @JsonIgnore
    private Set<Seat> seats = new HashSet<>();

    // ---------------- DISPLAY / INFO ----------------
    private String userName;
    private String userEmail;
    private String concertTitle;
    private String seatNumber;

    private String deliveryMethod;
    private String shippingAddress;
    private String userIdNumber;
    private String proofUrl;

    @Column(length = 2000)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // ---------------- TIMESTAMPS ----------------
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            outTradeNo = "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
        }
    }

    // ---------------- GETTERS / SETTERS ----------------
    public Long getId() { return id; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Concert getConcert() { return concert; }
    public void setConcert(Concert concert) { this.concert = concert; }

    public Set<Seat> getSeats() { return seats; }
    public void setSeats(Set<Seat> seats) { this.seats = seats; }
    public void addSeat(Seat seat) { this.seats.add(seat); }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getUserIdNumber() { return userIdNumber; }
    public void setUserIdNumber(String userIdNumber) { this.userIdNumber = userIdNumber; }

    public String getProofUrl() { return proofUrl; }
    public void setProofUrl(String proofUrl) { this.proofUrl = proofUrl; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getConcertTitle() { return concertTitle; }
    public void setConcertTitle(String concertTitle) { this.concertTitle = concertTitle; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}