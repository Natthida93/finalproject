package com.project.concert.service;

import com.project.concert.model.*;
import com.project.concert.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatService seatService;

    public BookingService(BookingRepository bookingRepository, SeatService seatService) {
        this.bookingRepository = bookingRepository;
        this.seatService = seatService;
    }

    /**
     * Create booking by locking the seat first.
     * Seat will remain LOCKED until payment confirms booking.
     */
    @Transactional
    public Booking createBooking(User user, Seat seat) {

        // Check if seat is already booked
        if (seat.getStatus() == SeatStatus.BOOKED) {
            throw new RuntimeException("Seat already booked");
        }

        // If seat is locked by another user, block
        if (seat.getStatus() == SeatStatus.LOCKED &&
                !user.getId().equals(seat.getLockedById()) &&
                seat.getLockedUntil() != null &&
                seat.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Seat is locked by another user");
        }

        // Lock seat for this user (5 minutes)
        seat.setStatus(SeatStatus.LOCKED);
        seat.setLockedById(user.getId());
        seat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        seatService.saveSeat(seat);

        // Create booking record with PENDING status
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setConcert(seat.getConcert());
        booking.setSeatId(seat.getId());
        booking.setSeatNumber(seat.getSeatNumber());
        booking.setZoneName(seat.getSection().getName());
        booking.setTotalPrice(seat.getPrice()); // seat already has correct price
        booking.setStatus("PENDING"); // will become CONFIRMED after payment

        return bookingRepository.save(booking);
    }

    /**
     * Confirm booking after successful payment
     */
    @Transactional
    public Booking confirmBooking(Booking booking) {
        Seat seat = seatService.getSeatById(booking.getSeatId());

        if (!booking.getUser().getId().equals(seat.getLockedById())) {
            throw new RuntimeException("Cannot confirm booking: seat locked by another user");
        }

        seat.setStatus(SeatStatus.BOOKED);
        seat.setLockedById(null);
        seat.setLockedUntil(null);
        seatService.saveSeat(seat);

        booking.setStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    /**
     * Cancel booking (release seat)
     */
    @Transactional
    public void cancelBooking(Booking booking) {
        Seat seat = seatService.getSeatById(booking.getSeatId());

        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setLockedById(null);
        seat.setLockedUntil(null);
        seatService.saveSeat(seat);

        bookingRepository.delete(booking);
    }
}