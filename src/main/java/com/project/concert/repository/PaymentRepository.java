package com.project.concert.repository;

import com.project.concert.model.Payment;
import com.project.concert.model.User;
import com.project.concert.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByUserAndConcert(User user, Concert concert);
}
