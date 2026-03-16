package com.project.concert.dto;

public class SeatDto {
    private Long seatId;
    private String label;
    private String status;

    private Double price;

    public SeatDto(Long seatId, String label, String status, Double price) {
        this.seatId = seatId;
        this.label = label;
        this.status = status;
        this.price = price;
    }

    public Long getSeatId() { return seatId; }
    public String getLabel() { return label; }
    public String getStatus() { return status; }
    public Double getPrice() { return price; }
}