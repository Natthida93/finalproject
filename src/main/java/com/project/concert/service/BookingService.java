package com.project.concert.service;

import com.project.concert.model.*;
import com.project.concert.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatService seatService;

    public BookingService(BookingRepository bookingRepository, SeatService seatService) {
        this.bookingRepository = bookingRepository;
        this.seatService = seatService;
    }

    /*** Create booking from a set of seats and a payment */
    @Transactional
    public Booking createBooking(User user, Concert concert, Set<Seat> seats, Payment payment) {

        // Check seats availability
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new RuntimeException("Seat already booked: " + seat.getSeatNumber());
            }
            // Optional: check if locked by this user
            if (seat.getStatus() == SeatStatus.LOCKED &&
                    !user.getId().equals(seat.getLockedById()) &&
                    seat.getLockedUntil() != null &&
                    seat.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new RuntimeException("Seat is locked by another user: " + seat.getSeatNumber());
            }

            // Mark seat as booked
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedById(null);
            seat.setLockedUntil(null);
            seatService.saveSeat(seat);
        }

        // Create booking record
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setConcert(concert);
        booking.setSeats(seats);
        booking.setPayment(payment);

        return bookingRepository.save(booking);
    }

    /*** Cancel a booking (release seats)*/
    @Transactional
    public void cancelBooking(Booking booking) {
        Set<Seat> seats = booking.getSeats();
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedById(null);
            seat.setLockedUntil(null);
            seatService.saveSeat(seat);
        }
        bookingRepository.delete(booking);
    }
}