package com.project.concert.service;

import com.project.concert.model.Seat;
import com.project.concert.model.SeatStatus;
import com.project.concert.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatSchedulerService {

    private final SeatRepository seatRepository;

    public SeatSchedulerService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredSeatLocks() {
        seatRepository.releaseExpiredLocks(LocalDateTime.now());
    }
}

