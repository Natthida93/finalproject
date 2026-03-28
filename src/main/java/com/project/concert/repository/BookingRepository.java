package com.project.concert.repository;

import com.project.concert.model.Booking;
import com.project.concert.model.Concert;
import com.project.concert.model.Payment;
import com.project.concert.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph; // ✅ ADD THIS
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByConcertId(Long concertId);

    List<Booking> findByUser(User user);

    List<Booking> findByUserAndConcert(User user, Concert concert);

    @Query("SELECT b FROM Booking b JOIN b.seats s WHERE s.id = :seatId")
    Optional<Booking> findBySeatInBooking(@Param("seatId") Long seatId);

    Optional<Booking> findByPayment(Payment payment);


    @EntityGraph(attributePaths = {
            "user",
            "concert",
            "concert.sections",
            "seats",
            "payment"
    })
    Optional<Booking> findByPayment_Id(Long paymentId);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.seats LEFT JOIN FETCH b.concert WHERE b.payment.id = :paymentId")
    Optional<Booking> findByPaymentIdWithSeatsAndConcert(@Param("paymentId") Long paymentId);


}