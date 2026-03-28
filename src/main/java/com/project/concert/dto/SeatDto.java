package com.project.concert.dto;
import java.math.BigDecimal;

public class SeatDto {

    private Long seatId;
    private String label;
    private String status;
    private BigDecimal price;

    public SeatDto(Long seatId, String label, String status, BigDecimal price) {
        this.seatId = seatId;
        this.label = label;
        this.status = status;
        this.price = price;
    }

    public Long getSeatId() {
        return seatId;
    }

    public String getLabel() {
        return label;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getPrice() {
        return price;
    }
}