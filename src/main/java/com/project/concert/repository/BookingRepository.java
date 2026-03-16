package com.project.concert.repository;

import com.project.concert.model.Booking;
import com.project.concert.model.Concert;
import com.project.concert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    // Get all bookings for a specific concert
    List<Booking> findByConcertId(Long concertId);

    // Check if a specific user already booked a specific concert
    Optional<Booking> findByUserAndConcert(User user, Concert concert); // prevent duplicate

    // Get all bookings for a specific user (for history)
    List<Booking> findByUser(User user);
}