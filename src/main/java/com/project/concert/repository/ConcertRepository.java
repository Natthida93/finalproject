package com.project.concert.repository;

import com.project.concert.model.Booking;
import com.project.concert.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConcertRepository extends JpaRepository<Concert, Long> {


    @Query("""
        SELECT c
        FROM Concert c
        LEFT JOIN FETCH c.sections
        WHERE c.id = :id
    """)
    Optional<Concert> findByIdWithSections(@Param("id") Long id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.seats LEFT JOIN FETCH b.concert WHERE b.payment.id = :paymentId")
    Optional<Booking> findByPaymentIdWithSeatsAndConcert(@Param("paymentId") Long paymentId);

}