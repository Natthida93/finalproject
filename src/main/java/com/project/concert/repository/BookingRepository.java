package com.project.concert.repository;

import com.project.concert.model.Booking;
import com.project.concert.model.Concert;
import com.project.concert.model.Payment;
import com.project.concert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Get all bookings for a specific concert
    List<Booking> findByConcertId(Long concertId);

    // Booking history for a user
    List<Booking> findByUser(User user);

    // Get bookings for user in a specific concert
    List<Booking> findByUserAndConcert(User user, Concert concert);

    // Check if a specific seat is already booked
    @Query("SELECT b FROM Booking b JOIN b.seats s WHERE s.id = :seatId")
    Optional<Booking> findBySeatInBooking(@Param("seatId") Long seatId);

    // Find booking by payment
    Optional<Booking> findByPayment(Payment payment);
}