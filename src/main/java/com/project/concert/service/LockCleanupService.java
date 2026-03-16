package com.project.concert.service;

import com.project.concert.repository.SeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LockCleanupService {

    private final SeatRepository seatRepository;

    public LockCleanupService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredLocks() {

        int updated = seatRepository.releaseExpiredLocks(LocalDateTime.now());

        if (updated > 0) {
            System.out.println("Released " + updated + " expired seat locks");
        }
    }
}