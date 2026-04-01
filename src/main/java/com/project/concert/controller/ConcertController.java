package com.project.concert.controller;

import com.project.concert.model.Concert;
import com.project.concert.repository.ConcertRepository;
import com.project.concert.service.ConcertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/concerts")
@CrossOrigin(origins = "https://concertticketingsystem.netlify.app")
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertRepository concertRepository;

    public ConcertController(ConcertService concertService,
                             ConcertRepository concertRepository) {
        this.concertService = concertService;
        this.concertRepository = concertRepository;
    }

    @PostMapping
    public Concert createConcert(@RequestBody Concert concert) {
        return concertService.createConcert(concert);
    }

    @GetMapping
    public List<Concert> getAllConcerts() {
        return concertRepository.findAll();
    }


    @GetMapping("/{id}")
    public Concert getConcertById(@PathVariable Long id) {
        return concertService.getConcertWithSections(id);
    }

    @GetMapping("/{id}/available-seats")
    public Map<String, Integer> getAvailableSeats(@PathVariable Long id) {
        return concertService.getAvailableSeatsByZone(id);
    }
}