package com.project.concert.repository;
import com.project.concert.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByConcert_Id(Long concertId);
}