package com.ooad.home4paws.Controller;

import com.ooad.home4paws.Entity.Report;
import com.ooad.home4paws.Service.ReportService;
import com.ooad.home4paws.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173") // allow Vite frontend
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    // CREATE a new report (requires authentication)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Report> createReport(@RequestPart("report") String reportJson,
                                                 @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        Report report;
        try {
            report = new ObjectMapper().readValue(reportJson, Report.class);
        } catch (IOException e) {
            System.err.println("Error deserializing report JSON: " + reportJson);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        
        // Get the current authenticated user's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Find user by username and set userId on the report
        userRepository.findByUsername(username).ifPresent(user -> {
            report.setUserId(user.getId());
        });
        
        System.out.println("Received report from user: " + username + " - " + report.getName());

        if (photos != null && !photos.isEmpty()) {
            List<String> photoPaths = reportService.storePhotos(photos);
            report.setPhotos(photoPaths);
        }

        Report savedReport = reportService.createReport(report);
        return ResponseEntity.ok(savedReport);
    }

    // GET all reports (returns empty for guests - use /my-reports for logged-in users)
    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        // Return empty list - guests should not see any reports
        return ResponseEntity.ok(Collections.emptyList());
    }
    
    // GET current user's reports (requires authentication)
    @GetMapping("/my-reports")
    public ResponseEntity<List<Report>> getMyReports() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(reportService.getReportsByUserId(user.getId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }

    // GET a single report by ID
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE an existing report
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Report> updateReport(@PathVariable Long id,
                                                 @RequestPart("report") String reportJson,
                                                 @RequestPart(value = "photos", required = false) List<MultipartFile> photos) throws IOException {
        Report updatedReport;
        try {
            updatedReport = new ObjectMapper().readValue(reportJson, Report.class);
        } catch (IOException e) {
            System.err.println("Error deserializing update report JSON: " + reportJson);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

        if (photos != null && !photos.isEmpty()) {
            List<String> photoPaths = reportService.storePhotos(photos);
            updatedReport.setPhotos(photoPaths);
        }
        
        try {
            Report report = reportService.updateReport(id, updatedReport);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Report not found")) {
                return ResponseEntity.notFound().build(); // Return 404 instead of 500
            }
            throw e; // Re-throw other runtime exceptions
        }
    }

    // DELETE a report
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);
        if (!deleted) {
            return ResponseEntity.notFound().build(); // return 404 instead of 500
        }
        return ResponseEntity.noContent().build(); // 204 on success
    }
}
