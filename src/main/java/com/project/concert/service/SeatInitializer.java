package com.project.concert.service;


import com.project.concert.service.SeatService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SeatInitializer implements CommandLineRunner {

    private final SeatService seatService;

    public SeatInitializer(SeatService seatService) {
        this.seatService = seatService;
    }

    @Override
    public void run(String... args) throws Exception {
        seatService.generateSeatsForAllConcerts();
        System.out.println("Seats generated for all concerts if missing!");
    }
}