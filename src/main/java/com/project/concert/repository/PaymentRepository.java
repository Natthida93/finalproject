package com.project.concert.repository;

import com.project.concert.model.Payment;
import com.project.concert.model.PaymentStatus;
import com.project.concert.model.User;
import com.project.concert.model.Concert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ---------------- BASIC ----------------
    Optional<Payment> findByUserAndConcert(User user, Concert concert);

    List<Payment> findAllByUserAndConcert(User user, Concert concert);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByUserAndConcertAndStatus(User user, Concert concert, PaymentStatus status);

    // ---------------- NEW (FIX YOUR ERROR) ----------------
    // ✅ REQUIRED for Alipay callback
    Optional<Payment> findByOutTradeNo(String outTradeNo);

    // ---------------- USED IN CONTROLLER ----------------
    List<Payment> findByUserIdAndConcertId(Long userId, Long concertId);

    List<Payment> findByUserEmail(String email);

    // VERY IMPORTANT (used for CONTINUE PAYMENT)
    @Query("""
        SELECT DISTINCT p
        FROM Payment p
        LEFT JOIN FETCH p.seats
        LEFT JOIN FETCH p.concert
        WHERE p.user.id = :userId
        AND p.concert.id = :concertId
        AND p.status = 'PENDING'
    """)
    List<Payment> findPendingPaymentsWithSeats(@Param("userId") Long userId,
                                               @Param("concertId") Long concertId);

    // ---------------- ADMIN ----------------
    @Query("""
        SELECT p
        FROM Payment p
        JOIN FETCH p.user
        JOIN FETCH p.concert
    """)
    List<Payment> findAllWithUserAndConcert();

    // USED when you need seats + concert together
    @Query("""
        SELECT DISTINCT p
        FROM Payment p
        LEFT JOIN FETCH p.seats
        LEFT JOIN FETCH p.concert
        WHERE p.user.id = :userId
        AND p.concert.id = :concertId
    """)
    List<Payment> findByUserIdAndConcertIdWithSeats(@Param("userId") Long userId,
                                                    @Param("concertId") Long concertId);

    // THIS ONE FIXES LAZY LOAD ISSUES
    @Query("SELECT p FROM Payment p " +
            "LEFT JOIN FETCH p.seats " +
            "LEFT JOIN FETCH p.concert " +
            "WHERE p.id = :id")
    Optional<Payment> findByIdWithConcertAndSeats(@Param("id") Long id);

    @Query("SELECT p FROM Payment p JOIN p.seats s WHERE p.user.id = :userId AND s.id IN :seatIds AND p.status IN ('PENDING','COMPLETED')")
    Optional<Payment> findPendingOrCompletedByUserAndSeats(@Param("userId") Long userId, @Param("seatIds") List<Long> seatIds);
}