package com.project.concert.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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

    public void setId(Long id) {
        this.id = id;}

    // ===== Relationship to Concert =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    @JsonBackReference
    private Concert concert;

    // ===== Relationship to Seats =====
    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Seat> seats;

    public Section() {}

    public Section(String name, double priceMultiplier, Concert concert) {
        this.name = name;
        this.priceMultiplier = priceMultiplier;
        this.concert = concert;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}