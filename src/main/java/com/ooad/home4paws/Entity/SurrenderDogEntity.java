package com.ooad.home4paws.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "surrender_dogs")
public class SurrenderDogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surrenderId;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerPhone;

    @Column(nullable = false)
    private String ownerEmail;

    private String ownerAddress;

    @Column(nullable = false)
    private String dogName;

    private String dogBreed;

    private int dogAge;

    private String dogGender;

    private String dogSize;

    @Column(length = 2000)
    private String dogDescription;

    @JsonProperty("isVaccinated")
    private boolean isVaccinated;

    @JsonProperty("isNeutered")
    private boolean isNeutered;

    @JsonProperty("hasMedicalIssues")
    private boolean hasMedicalIssues;

    @Column(length = 1000)
    private String medicalHistory;

    @Column(length = 2000, nullable = false)
    private String surrenderReason;

    @JsonProperty("isUrgent")
    private boolean isUrgent;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate preferredDate;

    @Column(nullable = false)
    private String requestStatus;

    @Column(length = 1000)
    private String adminNotes;

    @Column(nullable = true)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "surrender_dog_photos", joinColumns = @JoinColumn(name = "surrender_id"))
    @Column(name = "photo_url")
    private List<String> dogPhotoUrls;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    private LocalDateTime lastUpdated;

    public SurrenderDogEntity() {
        this.requestStatus = "PENDING";
        this.submissionDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.dogPhotoUrls = new java.util.ArrayList<>();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getSurrenderId() {
        return surrenderId;
    }

    public void setSurrenderId(Long surrenderId) {
        this.surrenderId = surrenderId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    public String getDogName() {
        return dogName;
    }

    public void setDogName(String dogName) {
        this.dogName = dogName;
    }

    public String getDogBreed() {
        return dogBreed;
    }

    public void setDogBreed(String dogBreed) {
        this.dogBreed = dogBreed;
    }

    public int getDogAge() {
        return dogAge;
    }

    public void setDogAge(int dogAge) {
        this.dogAge = dogAge;
    }

    public String getDogGender() {
        return dogGender;
    }

    public void setDogGender(String dogGender) {
        this.dogGender = dogGender;
    }

    public String getDogSize() {
        return dogSize;
    }

    public void setDogSize(String dogSize) {
        this.dogSize = dogSize;
    }

    public String getDogDescription() {
        return dogDescription;
    }

    public void setDogDescription(String dogDescription) {
        this.dogDescription = dogDescription;
    }

    public boolean isVaccinated() {
        return isVaccinated;
    }

    public void setVaccinated(boolean vaccinated) {
        isVaccinated = vaccinated;
    }

    public boolean isNeutered() {
        return isNeutered;
    }

    public void setNeutered(boolean neutered) {
        isNeutered = neutered;
    }

    public boolean isHasMedicalIssues() {
        return hasMedicalIssues;
    }

    public void setHasMedicalIssues(boolean hasMedicalIssues) {
        this.hasMedicalIssues = hasMedicalIssues;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getSurrenderReason() {
        return surrenderReason;
    }

    public void setSurrenderReason(String surrenderReason) {
        this.surrenderReason = surrenderReason;
    }

    public boolean isUrgent() {
        return isUrgent;
    }

    public void setUrgent(boolean urgent) {
        isUrgent = urgent;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<String> getDogPhotoUrls() {
        return dogPhotoUrls;
    }

    public void setDogPhotoUrls(List<String> dogPhotoUrls) {
        this.dogPhotoUrls = dogPhotoUrls;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
