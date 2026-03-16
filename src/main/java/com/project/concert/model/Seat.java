package com.project.concert.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    public void setId(Long id) { this.id = id; }

    private Long lockedById;
    private String label;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private LocalDateTime lockedUntil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @JsonBackReference
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    @JsonIgnore
    private Concert concert;

    @ManyToMany(mappedBy = "seats")
    @JsonIgnore
    private Set<Payment> payments;

    public Seat() {}

    // ================= GETTERS / SETTERS =================
    public Long getId() { return id; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }

    public Concert getConcert() { return concert; }
    public void setConcert(Concert concert) { this.concert = concert; }

    public Set<Payment> getPayments() { return payments; }
    public void setPayments(Set<Payment> payments) { this.payments = payments; }

    public Long getLockedById() { return lockedById; }
    public void setLockedById(Long lockedById) { this.lockedById = lockedById; }
    public String getLabel() { return label; }
}