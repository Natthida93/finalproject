package com.project.concert.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private BigDecimal price;

    private String title;

    private String venue;

    private String image;

    private LocalDateTime startTime;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "available_seats")
    private Integer availableSeats;

    @OneToMany(mappedBy = "concert",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Section> sections = new ArrayList<>();

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public String getVenue() {
        return venue;
    }

    public String getImage() {
        return image;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public List<Section> getSections() {
        return sections;
    }

    // ================= SETTERS =================

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }


    public void setSections(List<Section> sections) {
        this.sections.clear();

        if (sections != null) {
            for (Section section : sections) {
                addSection(section);
            }
        }
    }

    // ================= RELATION HELPERS =================

    public void addSection(Section section) {
        this.sections.add(section);
        section.setConcert(this);
    }

    public void removeSection(Section section) {
        this.sections.remove(section);
        section.setConcert(null);
    }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
}