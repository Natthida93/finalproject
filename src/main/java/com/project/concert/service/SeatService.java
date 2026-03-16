package com.project.concert.service;

import com.project.concert.model.*;
import com.project.concert.repository.ConcertRepository;
import com.project.concert.repository.SeatRepository;
import com.project.concert.repository.SectionRepository;
import com.project.concert.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;

    public SeatService(SeatRepository seatRepository,
                       ConcertRepository concertRepository,
                       SectionRepository sectionRepository,
                       UserRepository userRepository) {
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
    }

    // ================= LOCK SEAT =================
    @Transactional
    public Seat lockSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        LocalDateTime now = LocalDateTime.now();

        if (seat.getStatus() == SeatStatus.BOOKED)
            throw new RuntimeException("Seat already booked");

        if (seat.getStatus() == SeatStatus.LOCKED &&
                seat.getLockedUntil() != null &&
                seat.getLockedUntil().isAfter(now) &&
                !userId.equals(seat.getLockedById())) {
            throw new RuntimeException("Seat is currently locked by another user");
        }

        seat.setStatus(SeatStatus.LOCKED);
        seat.setLockedById(userId);
        seat.setLockedUntil(now.plusMinutes(5));

        seatRepository.save(seat);
        return seat;
    }
    // ================= UNLOCK SEAT =================
    @Transactional
    public void unlockSeat(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));

        // Only the user who locked it can unlock
        if (!userId.equals(seat.getLockedById())) {
            throw new RuntimeException("You do not own this lock");
        }

        // Only unlock if currently locked
        if (seat.getStatus() == SeatStatus.LOCKED) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedById(null);
            seat.setLockedUntil(null);
            seatRepository.save(seat);
        }
    }
    // ================= CONFIRM BOOKING =================
    @Transactional
    public Seat confirmBooking(Long seatId, Long userId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        LocalDateTime now = LocalDateTime.now();

        if (seat.getStatus() != SeatStatus.LOCKED)
            throw new RuntimeException("Seat is not locked");
        if (!userId.equals(seat.getLockedById()))
            throw new RuntimeException("You did not lock this seat");
        if (seat.getLockedUntil() == null || seat.getLockedUntil().isBefore(now))
            throw new RuntimeException("Lock expired");

        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedById(null);
        seat.setLockedUntil(null);

        seatRepository.save(seat);

        // update available seats in concert
        Concert concert = seat.getConcert();
        concert.setAvailableSeats(concert.getAvailableSeats() - 1);

        return seat;
    }

    // ================= SAVE SEAT =================
    @Transactional
    public void saveSeat(Seat seat) {
        seatRepository.save(seat);
    }

    // ================= GET SEATS =================
    @Transactional
    public List<Seat> getSeatsByConcert(Long concertId) {
        return seatRepository.findByConcertIdWithSection(concertId);
    }

    // ================= RELEASE EXPIRED LOCKS =================
    @Transactional
    public int releaseExpiredLocks(LocalDateTime now) {
        return seatRepository.releaseExpiredLocks(now);
    }

    // ================= GET USER =================
    @Transactional
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    // ================= GENERATE SEATS =================
    @Transactional
    public void generateSeatsForConcert(Concert concert) {
        Section vip = new Section("VIP", 2.0, concert);
        Section a = new Section("A", 1.5, concert);
        Section b = new Section("B", 1.2, concert);
        Section c = new Section("C", 1.0, concert);

        sectionRepository.saveAndFlush(vip);
        sectionRepository.saveAndFlush(a);
        sectionRepository.saveAndFlush(b);
        sectionRepository.saveAndFlush(c);

        createSeatsForSection(concert, vip, 2, 20);
        createSeatsForSection(concert, a, 5, 20);
        createSeatsForSection(concert, b, 8, 20);
        createSeatsForSection(concert, c, 10, 20);
    }

    private void createSeatsForSection(Concert concert, Section section, int rows, int seatsPerRow) {
        for (int r = 0; r < rows; r++) {
            char rowLetter = (char) ('A' + r);
            for (int s = 1; s <= seatsPerRow; s++) {
                Seat seat = new Seat();
                seat.setConcert(concert);
                seat.setSection(section);
                seat.setSeatNumber(rowLetter + String.valueOf(s));
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setPrice(concert.getPrice() * section.getPriceMultiplier());
                seatRepository.save(seat);
            }
        }
    }

    // ================= GENERATE ALL CONCERTS =================
    @Transactional
    public void generateSeatsForAllConcerts() {
        List<Concert> concerts = concertRepository.findAll();
        for (Concert concert : concerts) {
            if (!seatRepository.findByConcert_Id(concert.getId()).isEmpty()) continue;
            generateSeatsForConcert(concert);
        }
    }

    @Transactional
    public Seat getSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
    }

    public List<Seat> validateSeatLocks(List<Integer> seatIds, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Seat> validSeats = new ArrayList<>();
        for (Integer id : seatIds) {
            Seat seat = seatRepository.findById(Long.valueOf(id)).orElse(null);
            if (seat != null && seat.getStatus() == SeatStatus.LOCKED
                    && user.getId().equals(seat.getLockedById())) {
                validSeats.add(seat);
            }
        }
        return validSeats;
    }
}