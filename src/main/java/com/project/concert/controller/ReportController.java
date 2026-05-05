package com.project.concert.controller;

import com.project.concert.model.Report;
import com.project.concert.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    // ================= CREATE REPORT =================
    @PostMapping("/report")
    public ResponseEntity<String> submitReport(@RequestBody Report report) {

        report.setCreatedAt(LocalDateTime.now());

        reportRepository.save(report);

        return ResponseEntity.ok("Report submitted successfully");
    }

    // ================= GET ALL REPORTS (ADMIN) =================
    @GetMapping("/admin/reports")
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportRepository.findAll());
    }

    // ================= DELETE REPORT (ADMIN) =================
    @DeleteMapping("/admin/reports/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable Long id) {

        if (!reportRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        reportRepository.deleteById(id);
        return ResponseEntity.ok("Report deleted");
    }
}