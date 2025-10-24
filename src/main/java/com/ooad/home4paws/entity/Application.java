package com.ooad.home4paws.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotNull(message = "Dog ID is required")
    @Column(name = "dog_id", nullable = false)
    private Long dogId;
    
    @NotNull(message = "Application type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationType type;
    
    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;
    
    @Column(name = "message")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "admin_notes")
    private String adminNotes;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Constructors
    public Application() {
        this.submittedAt = LocalDateTime.now();
    }
    
    public Application(Long userId, Long dogId, ApplicationType type, String fullName, String email, String phoneNumber, String address, String message) {
        this();
        this.userId = userId;
        this.dogId = dogId;
        this.type = type;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.message = message;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getDogId() {
        return dogId;
    }
    
    public void setDogId(Long dogId) {
        this.dogId = dogId;
    }
    
    public ApplicationType getType() {
        return type;
    }
    
    public void setType(ApplicationType type) {
        this.type = type;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
