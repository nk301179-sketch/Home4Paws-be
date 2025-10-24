package com.ooad.home4paws.Service;

import com.ooad.home4paws.Entity.Report;
import com.ooad.home4paws.Repository.ReportRepository;
import com.ooad.home4paws.entity.ReportStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    private final String UPLOAD_DIR = "./uploads/reports";

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report createReport(Report report) {
        // Set submitted time and default status
        report.setSubmittedAt(LocalDateTime.now());
        report.setStatus(ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    public List<String> storePhotos(List<MultipartFile> files) throws IOException {
        // Validate photo count limit
        if (files != null && files.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 photos allowed per report");
        }
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> photoPaths = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(file.getInputStream(), filePath);
                photoPaths.add("/uploads/reports/" + uniqueFileName);
            }
        }
        return photoPaths;
    }

    private void deletePhotosFromServer(List<String> photoUrls) {
        if (photoUrls != null && !photoUrls.isEmpty()) {
            System.out.println("Attempting to delete " + photoUrls.size() + " photo files...");
            for (String photoUrl : photoUrls) {
                try {
                    // Handle both full paths and just filenames
                    String filename;
                    if (photoUrl.contains("/")) {
                        filename = Paths.get(photoUrl).getFileName().toString();
                    } else {
                        filename = photoUrl;
                    }
                    
                    Path filePath = Paths.get(UPLOAD_DIR, filename);
                    
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        System.out.println("Successfully deleted photo file: " + filePath);
                    } else {
                        System.out.println("Photo file not found (may already be deleted): " + filePath);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete photo file: " + photoUrl + ". Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error deleting photo file: " + photoUrl + ". Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("No photos to delete for this report.");
        }
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }
    
    public List<Report> getReportsByUserId(Long userId) {
        return reportRepository.findByUserId(userId);
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public Report updateReport(Long id, Report updatedReport) {
        return reportRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedReport.getName());
                    existing.setPhone(updatedReport.getPhone());
                    existing.setDescription(updatedReport.getDescription());
                    existing.setLocation(updatedReport.getLocation());
                    existing.setPhotos(updatedReport.getPhotos()); // Update to setPhotos
                    return reportRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Report not found with id " + id));
    }

    public boolean deleteReport(Long id) {
        try {
            // First, get the report to access its photos before deletion
            Optional<Report> reportOptional = reportRepository.findById(id);
            if (reportOptional.isEmpty()) {
                return false; // report not found
            }
            
            Report report = reportOptional.get();
            
            // Delete associated photo files from the server
            deletePhotosFromServer(report.getPhotos());
            
            // Now delete the report from the database
            reportRepository.deleteById(id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false; // report not found
        } catch (Exception e) {
            System.err.println("Error deleting report with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false; // Indicate deletion failed due to an unexpected error
        }
    }

    public Report updateReportStatus(Long id, ReportStatus status) {
        return reportRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(status);
                    return reportRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Report not found with id " + id));
    }
}
