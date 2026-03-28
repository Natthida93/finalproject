package com.project.concert.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VIP / A / B / C
    private String name;

    // VIP = 2.0 , A = 1.5 , B = 1.2 , C = 1.0
    private double priceMultiplier;

    // ===== Relationship to Concert =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    @JsonBackReference
    private Concert concert;

    // ===== Relationship to Seats =====
    @OneToMany(mappedBy = "section",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Seat> seats = new ArrayList<>();

    public Section() {}

    public Section(String name, double priceMultiplier, Concert concert) {
        this.name = name;
        this.priceMultiplier = priceMultiplier;
        this.concert = concert;
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public Concert getConcert() {
        return concert;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    // ================= SETTERS =================

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    // ✅ FIXED (same pattern as Concert)
    public void setSeats(List<Seat> seats) {
        this.seats.clear();

        if (seats != null) {
            for (Seat seat : seats) {
                addSeat(seat);
            }
        }
    }

    // ================= RELATION HELPERS =================

    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.setSection(this); // 🔥 VERY IMPORTANT
    }

    public void removeSeat(Seat seat) {
        this.seats.remove(seat);
        seat.setSection(null);
    }
}