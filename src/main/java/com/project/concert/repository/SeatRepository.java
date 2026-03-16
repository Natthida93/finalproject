package com.project.concert.repository;

import com.project.concert.model.Seat;
import com.project.concert.model.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // ================= GET SEATS BY SECTION =================
    List<Seat> findBySection_Id(Long sectionId);

    // ================= GET SEATS BY CONCERT =================
    List<Seat> findByConcert_Id(Long concertId);

    List<Seat> findAllByIdInAndStatusAndLockedById(List<Long> ids, SeatStatus status, Long lockedById);

    // ================= COUNT AVAILABLE SEATS =================
    int countBySection_IdAndStatus(Long sectionId, SeatStatus status);

    // ================= PESSIMISTIC LOCK FOR UPDATE =================
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);

    // ================= RELEASE EXPIRED LOCKS =================
    @Modifying
    @Query("""
        UPDATE Seat s
        SET s.status = com.project.concert.model.SeatStatus.AVAILABLE,
            s.lockedById = NULL,
            s.lockedUntil = NULL
        WHERE s.status = com.project.concert.model.SeatStatus.LOCKED
        AND s.lockedUntil < :now
    """)
    int releaseExpiredLocks(@Param("now") LocalDateTime now);

    // ================= FETCH SEATS WITH SECTION (EAGER) =================
    @Query("SELECT s FROM Seat s JOIN FETCH s.section WHERE s.concert.id = :concertId")
    List<Seat> findByConcertIdWithSection(@Param("concertId") Long concertId);

}