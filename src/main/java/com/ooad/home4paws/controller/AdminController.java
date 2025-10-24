package com.ooad.home4paws.controller;

import com.ooad.home4paws.dto.LoginRequest;
import com.ooad.home4paws.entity.User;
import com.ooad.home4paws.entity.Dog;
import com.ooad.home4paws.entity.Application;
import com.ooad.home4paws.entity.ContactMessage;
import com.ooad.home4paws.entity.ContactMessageStatus;
import com.ooad.home4paws.entity.ReportStatus;
import com.ooad.home4paws.Entity.Report;
import com.ooad.home4paws.Entity.SurrenderDogEntity;
import com.ooad.home4paws.service.AdminService;
import com.ooad.home4paws.service.UserService;
import com.ooad.home4paws.service.DogService;
import com.ooad.home4paws.service.ApplicationService;
import com.ooad.home4paws.service.ContactMessageService;
import com.ooad.home4paws.Service.ReportService;
import com.ooad.home4paws.Service.SurrenderDogService;
import com.ooad.home4paws.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin authentication and management endpoints")
public class AdminController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private SurrenderDogService surrenderDogService;

    @Autowired
    private ContactMessageService contactMessageService;

    @Autowired
    private DogService dogService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JwtUtils jwtUtils;

    @Operation(summary = "Admin login", description = "Authenticate admin user and receive JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or not an admin")
    })
    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest req) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            
            // Check if the user is an admin
            if (!adminService.isAdmin(req.getUsername())) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            var principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            List<String> roles = principal.getAuthorities()
                                          .stream()
                                          .map(a -> a.getAuthority())
                                          .collect(Collectors.toList());

            String jwt = jwtUtils.generateToken(principal.getUsername(), roles);

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "username", principal.getUsername(),
                    "roles", roles,
                    "message", "Admin login successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Invalid credentials"
            ));
        }
    }

    @Operation(summary = "Get admin profile", description = "Get current admin user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Admin profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getAdminProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            User admin = adminService.getAdminByUsername(username);
            if (admin == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Admin not found"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "email", admin.getEmail(),
                "firstName", admin.getFirstName(),
                "lastName", admin.getLastName(),
                "enabled", admin.isEnabled()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Get admin profile (me)", description = "Get current admin user information - alias for /profile")
    @GetMapping("/me")
    public ResponseEntity<?> getAdminMe() {
        return getAdminProfile();
    }

    @Operation(summary = "Check admin status", description = "Check if current user is an admin")
    @GetMapping("/check")
    public ResponseEntity<?> checkAdminStatus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            boolean isAdmin = adminService.isAdmin(username);
            
            return ResponseEntity.ok(Map.of(
                "isAdmin", isAdmin,
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    // ========== USER MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all users", description = "Get list of all users in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<User> users = userService.findAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Delete user", description = "Delete a user account by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                "message", "User deleted successfully",
                "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete user: " + e.getMessage()
            ));
        }
    }

    // ========== REPORT MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all reports", description = "Get list of all reports in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reports retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<Report> reports = reportService.getAllReports();
            
            // Enhance reports with user information
            List<Map<String, Object>> enhancedReports = reports.stream().map(report -> {
                Map<String, Object> reportData = new HashMap<>();
                reportData.put("id", report.getId());
                reportData.put("name", report.getName());
                reportData.put("phone", report.getPhone());
                reportData.put("description", report.getDescription());
                reportData.put("location", report.getLocation());
                reportData.put("photos", report.getPhotos());
                reportData.put("userId", report.getUserId());
                reportData.put("status", report.getStatus());
                reportData.put("submittedAt", report.getSubmittedAt());
                
                // Add user account information if userId exists
                if (report.getUserId() != null) {
                    try {
                        var user = userService.findById(report.getUserId());
                        if (user.isPresent()) {
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("id", user.get().getId());
                            userInfo.put("username", user.get().getUsername());
                            userInfo.put("email", user.get().getEmail());
                            userInfo.put("firstName", user.get().getFirstName());
                            userInfo.put("lastName", user.get().getLastName());
                            reportData.put("user", userInfo);
                        } else {
                            reportData.put("user", null);
                        }
                    } catch (Exception e) {
                        reportData.put("user", null);
                    }
                } else {
                    reportData.put("user", null);
                }
                
                return reportData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(enhancedReports);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Delete report", description = "Delete a report by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            boolean deleted = reportService.deleteReport(reportId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "message", "Report deleted successfully",
                    "reportId", reportId
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Report not found or could not be deleted"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete report: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Update report status", description = "Update the status of a report (PENDING, TOOK_ACTION, RESOLVED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Report status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<?> updateReportStatus(@PathVariable Long reportId, @RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            String statusStr = request.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Status is required"
                ));
            }

            ReportStatus status;
            try {
                status = ReportStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status. Must be one of: PENDING, TOOK_ACTION, RESOLVED"
                ));
            }

            Report updatedReport = reportService.updateReportStatus(reportId, status);
            return ResponseEntity.ok(Map.of(
                "message", "Report status updated successfully",
                "reportId", reportId,
                "status", updatedReport.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", "Report not found"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update report status: " + e.getMessage()
            ));
        }
    }

    // ========== SURRENDER SUBMISSION MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all surrender submissions", description = "Get list of all surrender submissions in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surrender submissions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/surrender-submissions")
    public ResponseEntity<?> getAllSurrenderSubmissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<SurrenderDogEntity> submissions = surrenderDogService.getAll();
            
            // Enhance submissions with user information
            List<Map<String, Object>> enhancedSubmissions = submissions.stream().map(submission -> {
                Map<String, Object> submissionData = new HashMap<>();
                submissionData.put("surrenderId", submission.getSurrenderId());
                submissionData.put("ownerName", submission.getOwnerName());
                submissionData.put("ownerEmail", submission.getOwnerEmail());
                submissionData.put("ownerPhone", submission.getOwnerPhone());
                submissionData.put("ownerAddress", submission.getOwnerAddress());
                submissionData.put("dogName", submission.getDogName());
                submissionData.put("dogBreed", submission.getDogBreed());
                submissionData.put("dogAge", submission.getDogAge());
                submissionData.put("dogGender", submission.getDogGender());
                submissionData.put("dogSize", submission.getDogSize());
                submissionData.put("dogDescription", submission.getDogDescription());
                submissionData.put("isNeutered", submission.isNeutered());
                submissionData.put("isVaccinated", submission.isVaccinated());
                submissionData.put("hasMedicalIssues", submission.isHasMedicalIssues());
                submissionData.put("medicalHistory", submission.getMedicalHistory());
                submissionData.put("surrenderReason", submission.getSurrenderReason());
                submissionData.put("isUrgent", submission.isUrgent());
                submissionData.put("preferredDate", submission.getPreferredDate());
                submissionData.put("requestStatus", submission.getRequestStatus());
                submissionData.put("submissionDate", submission.getSubmissionDate());
                submissionData.put("lastUpdated", submission.getLastUpdated());
                submissionData.put("adminNotes", submission.getAdminNotes());
                submissionData.put("userId", submission.getUserId());
                
                // Add user account information if userId exists
                if (submission.getUserId() != null) {
                    try {
                        var user = userService.findById(submission.getUserId());
                        if (user.isPresent()) {
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("id", user.get().getId());
                            userInfo.put("username", user.get().getUsername());
                            userInfo.put("email", user.get().getEmail());
                            userInfo.put("firstName", user.get().getFirstName());
                            userInfo.put("lastName", user.get().getLastName());
                            submissionData.put("user", userInfo);
                        } else {
                            submissionData.put("user", null);
                        }
                    } catch (Exception e) {
                        submissionData.put("user", null);
                    }
                } else {
                    submissionData.put("user", null);
                }
                
                return submissionData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(enhancedSubmissions);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Delete surrender submission", description = "Delete a surrender submission by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surrender submission deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Surrender submission not found")
    })
    @DeleteMapping("/surrender-submissions/{submissionId}")
    public ResponseEntity<?> deleteSurrenderSubmission(@PathVariable Long submissionId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            surrenderDogService.deleteSurrenderRequest(submissionId);
            return ResponseEntity.ok(Map.of(
                "message", "Surrender submission deleted successfully",
                "submissionId", submissionId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete surrender submission: " + e.getMessage()
            ));
        }
    }

    // ========== DOG MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all dogs", description = "Get list of all dogs in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dogs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/dogs")
    public ResponseEntity<?> getAllDogs() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<Dog> dogs = dogService.getAllDogs();
            return ResponseEntity.ok(dogs);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Create new dog", description = "Add a new dog to the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dog created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/dogs")
    public ResponseEntity<?> createDog(@Valid @RequestBody Dog dog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            Dog createdDog = dogService.createDog(dog);
            return ResponseEntity.ok(createdDog);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to create dog: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Update dog", description = "Update an existing dog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dog updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Dog not found")
    })
    @PutMapping("/dogs/{dogId}")
    public ResponseEntity<?> updateDog(@PathVariable Long dogId, @Valid @RequestBody Dog dog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            Dog updatedDog = dogService.updateDog(dogId, dog);
            if (updatedDog != null) {
                return ResponseEntity.ok(updatedDog);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Dog not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update dog: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Delete dog", description = "Delete a dog by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dog deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Dog not found")
    })
    @DeleteMapping("/dogs/{dogId}")
    public ResponseEntity<?> deleteDog(@PathVariable Long dogId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            boolean deleted = dogService.deleteDog(dogId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "message", "Dog deleted successfully",
                    "dogId", dogId
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Dog not found or could not be deleted"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete dog: " + e.getMessage()
            ));
        }
    }

    // ========== APPLICATION MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all applications", description = "Get list of all applications in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/applications")
    public ResponseEntity<?> getAllApplications() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<Application> applications = applicationService.getAllApplications();
            
            // Enhance applications with dog details
            List<Map<String, Object>> enhancedApplications = applications.stream().map(app -> {
                Map<String, Object> enhancedApp = new java.util.HashMap<>();
                enhancedApp.put("id", app.getId());
                enhancedApp.put("userId", app.getUserId());
                enhancedApp.put("dogId", app.getDogId());
                enhancedApp.put("type", app.getType());
                enhancedApp.put("fullName", app.getFullName());
                enhancedApp.put("email", app.getEmail());
                enhancedApp.put("phoneNumber", app.getPhoneNumber());
                enhancedApp.put("address", app.getAddress());
                enhancedApp.put("message", app.getMessage());
                enhancedApp.put("status", app.getStatus());
                enhancedApp.put("adminNotes", app.getAdminNotes());
                enhancedApp.put("submittedAt", app.getSubmittedAt());
                enhancedApp.put("processedAt", app.getProcessedAt());
                
                // Get dog details
                try {
                    var dog = dogService.getDogById(app.getDogId());
                    if (dog.isPresent()) {
                        Map<String, Object> dogDetails = new java.util.HashMap<>();
                        dogDetails.put("id", dog.get().getId());
                        dogDetails.put("name", dog.get().getName());
                        dogDetails.put("breed", dog.get().getBreed());
                        dogDetails.put("description", dog.get().getDescription());
                        dogDetails.put("price", dog.get().getPrice());
                        dogDetails.put("status", dog.get().getStatus());
                        dogDetails.put("isStray", dog.get().getIsStray());
                        dogDetails.put("image", dog.get().getImage());
                        enhancedApp.put("dog", dogDetails);
                    } else {
                        enhancedApp.put("dog", null);
                    }
                } catch (Exception e) {
                    enhancedApp.put("dog", null);
                }
                
                // Add user account information if userId exists
                if (app.getUserId() != null) {
                    try {
                        var user = userService.findById(app.getUserId());
                        if (user.isPresent()) {
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("id", user.get().getId());
                            userInfo.put("username", user.get().getUsername());
                            userInfo.put("email", user.get().getEmail());
                            userInfo.put("firstName", user.get().getFirstName());
                            userInfo.put("lastName", user.get().getLastName());
                            enhancedApp.put("user", userInfo);
                        } else {
                            enhancedApp.put("user", null);
                        }
                    } catch (Exception e) {
                        enhancedApp.put("user", null);
                    }
                } else {
                    enhancedApp.put("user", null);
                }
                
                return enhancedApp;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(enhancedApplications);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Update application status", description = "Update the status of an application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PutMapping("/applications/{applicationId}")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId, @RequestBody Map<String, Object> updateData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            Application application = new Application();
            application.setStatus(com.ooad.home4paws.entity.ApplicationStatus.valueOf(updateData.get("status").toString()));
            application.setAdminNotes(updateData.get("adminNotes") != null ? updateData.get("adminNotes").toString() : null);

            Application updatedApplication = applicationService.updateApplication(applicationId, application);
            if (updatedApplication != null) {
                return ResponseEntity.ok(updatedApplication);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Application not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update application: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Delete application", description = "Delete an application by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long applicationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            boolean deleted = applicationService.deleteApplication(applicationId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "message", "Application deleted successfully",
                    "applicationId", applicationId
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Application not found or could not be deleted"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete application: " + e.getMessage()
            ));
        }
    }

    // ========== CONTACT MESSAGE MANAGEMENT ENDPOINTS ==========

    @Operation(summary = "Get all contact messages", description = "Get list of all contact messages in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact messages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin")
    })
    @GetMapping("/contact-messages")
    public ResponseEntity<?> getAllContactMessages() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            List<ContactMessage> messages = contactMessageService.getAllContactMessages();
            
            // Enhance messages with user information
            List<Map<String, Object>> enhancedMessages = messages.stream().map(message -> {
                Map<String, Object> messageData = new HashMap<>();
                messageData.put("id", message.getId());
                messageData.put("name", message.getName());
                messageData.put("email", message.getEmail());
                messageData.put("message", message.getMessage());
                messageData.put("status", message.getStatus());
                messageData.put("adminResponse", message.getAdminResponse());
                messageData.put("submittedAt", message.getSubmittedAt());
                messageData.put("respondedAt", message.getRespondedAt());
                messageData.put("userId", message.getUserId());
                
                // Add user account information if userId exists
                if (message.getUserId() != null) {
                    try {
                        var user = userService.findById(message.getUserId());
                        if (user.isPresent()) {
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("id", user.get().getId());
                            userInfo.put("username", user.get().getUsername());
                            userInfo.put("email", user.get().getEmail());
                            userInfo.put("firstName", user.get().getFirstName());
                            userInfo.put("lastName", user.get().getLastName());
                            messageData.put("user", userInfo);
                        } else {
                            messageData.put("user", null);
                        }
                    } catch (Exception e) {
                        messageData.put("user", null);
                    }
                } else {
                    messageData.put("user", null);
                }
                
                return messageData;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(enhancedMessages);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Get contact message by ID", description = "Get a specific contact message by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact message retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Contact message not found")
    })
    @GetMapping("/contact-messages/{messageId}")
    public ResponseEntity<?> getContactMessage(@PathVariable Long messageId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            var message = contactMessageService.getContactMessageById(messageId);
            if (message.isPresent()) {
                return ResponseEntity.ok(message.get());
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Contact message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized"
            ));
        }
    }

    @Operation(summary = "Respond to contact message", description = "Add admin response to a contact message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Response added successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Contact message not found")
    })
    @PutMapping("/contact-messages/{messageId}/respond")
    public ResponseEntity<?> respondToContactMessage(@PathVariable Long messageId, @RequestBody Map<String, String> responseData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            String adminResponse = responseData.get("adminResponse");
            if (adminResponse == null || adminResponse.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Admin response is required"
                ));
            }

            ContactMessage updatedMessage = contactMessageService.respondToContactMessage(messageId, adminResponse);
            if (updatedMessage != null) {
                return ResponseEntity.ok(updatedMessage);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Contact message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to respond to contact message: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Update contact message status", description = "Update the status of a contact message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Contact message not found")
    })
    @PutMapping("/contact-messages/{messageId}/status")
    public ResponseEntity<?> updateContactMessageStatus(@PathVariable Long messageId, @RequestBody Map<String, String> statusData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            String statusStr = statusData.get("status");
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Status is required"
                ));
            }

            ContactMessageStatus status;
            try {
                status = ContactMessageStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status. Must be PENDING, RESPONDED, or CLOSED"
                ));
            }

            ContactMessage updatedMessage = contactMessageService.updateContactMessageStatus(messageId, status);
            if (updatedMessage != null) {
                return ResponseEntity.ok(updatedMessage);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Contact message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to update contact message status: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Delete contact message", description = "Delete a contact message by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact message deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - not an admin"),
        @ApiResponse(responseCode = "404", description = "Contact message not found")
    })
    @DeleteMapping("/contact-messages/{messageId}")
    public ResponseEntity<?> deleteContactMessage(@PathVariable Long messageId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (!adminService.isAdmin(username)) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Access denied. Admin privileges required."
                ));
            }

            boolean deleted = contactMessageService.deleteContactMessage(messageId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                    "message", "Contact message deleted successfully",
                    "messageId", messageId
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Contact message not found or could not be deleted"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to delete contact message: " + e.getMessage()
            ));
        }
    }
}