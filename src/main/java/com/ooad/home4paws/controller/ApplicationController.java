package com.ooad.home4paws.controller;

import com.ooad.home4paws.entity.Application;
import com.ooad.home4paws.service.ApplicationService;
import com.ooad.home4paws.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application endpoints for adoption and purchase requests")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private DogService dogService;

    @Operation(summary = "Submit application", description = "Submit a new adoption or purchase application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - login required")
    })
    @PostMapping
    public ResponseEntity<?> submitApplication(@Valid @RequestBody Application application) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required. Please login to submit an application."
                ));
            }
            
            // Get user ID from authentication - extract from the authenticated user
            String username = authentication.getName();
            // For now, we'll use a simple approach - in production, you'd extract from JWT claims
            Long userId = 1L; // This should be extracted from the user details
            
            // Verify the dog exists
            if (!dogService.getDogById(application.getDogId()).isPresent()) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Dog not found"
                ));
            }
            
            // Set the user ID from authentication
            application.setUserId(userId);
            Application createdApplication = applicationService.createApplication(application);
            
            return ResponseEntity.ok(Map.of(
                "message", "Application submitted successfully",
                "applicationId", createdApplication.getId(),
                "status", createdApplication.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to submit application: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get user applications", description = "Get all applications submitted by the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - login required")
    })
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required. Please login to view applications."
                ));
            }
            
            Long userId = 1L; // For now, using a default user ID
            List<Application> applications = applicationService.getApplicationsByUserId(userId);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve applications: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get application by ID", description = "Get a specific application by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - login required"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required. Please login to view application."
                ));
            }
            
            Optional<Application> application = applicationService.getApplicationById(id);
            if (application.isPresent()) {
                return ResponseEntity.ok(application.get());
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Application not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to retrieve application: " + e.getMessage()
            ));
        }
    }
}
