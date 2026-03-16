package com.project.concert.service;

import com.project.concert.model.Concert;
import com.project.concert.model.Seat;
import com.project.concert.model.SeatStatus;
import com.project.concert.model.Section;
import com.project.concert.repository.ConcertRepository;
import com.project.concert.repository.SeatRepository;
import com.project.concert.repository.SectionRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;

    public ConcertService(ConcertRepository concertRepository,
                          SeatRepository seatRepository,
                          SectionRepository sectionRepository) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional
    public Concert createConcert(Concert concert) {

        Concert savedConcert = concertRepository.save(concert);

        int totalSeats = savedConcert.getTotalSeats();
        int seatsPerRow = 20;

        List<Seat> seats = new ArrayList<>();

        char row = 'A';
        int seatNumber = 1;

        for (int i = 1; i <= totalSeats; i++) {

            Seat seat = new Seat();
            seat.setConcert(savedConcert);
            seat.setSeatNumber(row + String.valueOf(seatNumber));
            seat.setStatus(SeatStatus.AVAILABLE);

            seats.add(seat);

            seatNumber++;

            if (seatNumber > seatsPerRow) {
                seatNumber = 1;
                row++;
            }
        }

        seatRepository.saveAll(seats);

        return savedConcert;
    }

    @Transactional
    public Concert getConcertWithSections(Long concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("Concert not found"));

        // Fetch sections for this concert
        List<Section> sections = sectionRepository.findByConcert_Id(concertId);

        // For each section, fetch seats
        for (Section section : sections) {
            List<Seat> seats = seatRepository.findBySection_Id(section.getId());
            section.setSeats(seats);
        }

        concert.setSections(sections);

        return concert;
    }

    @Transactional
    public Map<String, Integer> getAvailableSeatsByZone(Long concertId) {
        // Fetch sections for the concert
        List<Section> sections = sectionRepository.findByConcert_Id(concertId);

        Map<String, Integer> availableSeats = new HashMap<>();

        for (Section section : sections) {
            // Count seats that are AVAILABLE
            int count = seatRepository.countBySection_IdAndStatus(section.getId(), SeatStatus.AVAILABLE);
            availableSeats.put(section.getName(), count);
        }

        return availableSeats;
    }
}