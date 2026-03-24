package com.project.concert.repository;

import com.project.concert.model.Payment;
import com.project.concert.model.User;
import com.project.concert.model.Concert;
import com.project.concert.model.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUserAndConcert(User user, Concert concert);

    List<Payment> findAllByUserAndConcert(User user, Concert concert);

    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByUserAndConcertAndStatus(User user, Concert concert, PaymentStatus status);

    /** Fetch seats with payment to avoid LazyInitializationException */
    @Query("""
        SELECT DISTINCT p
        FROM Payment p
        LEFT JOIN FETCH p.seats
        WHERE p.user.id = :userId
        AND p.concert.id = :concertId
        AND p.status = 'PENDING'
    """)
    List<Payment> findPendingPaymentsWithSeats(@Param("userId") Long userId,
                                               @Param("concertId") Long concertId);


    /*** Prevent duplicate payment creation when user refreshes payment page */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.user.id = :userId
        AND p.concert.id = :concertId
        AND p.seatNumber = :seatNumbers
        AND p.status = 'PENDING'
    """)
    Optional<Payment> findPendingPayment(@Param("userId") Long userId,
                                         @Param("concertId") Long concertId,
                                         @Param("seatNumbers") String seatNumbers);
}