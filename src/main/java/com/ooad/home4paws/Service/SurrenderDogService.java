package com.ooad.home4paws.Service;

import com.ooad.home4paws.Entity.SurrenderDogEntity;
import com.ooad.home4paws.Repository.SurrenderDogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.io.File;

@Service
public class SurrenderDogService {

    @Autowired
    private SurrenderDogRepository surrenderDogRepository;

    private final String UPLOAD_DIR = "./uploads/surrender-dogs";

    public List<String> storePhotos(List<MultipartFile> files) throws IOException {
        // Validate photo count limit
        if (files != null && files.size() > 5) {
            throw new IllegalArgumentException("Maximum 5 photos allowed per surrender request");
        }
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        List<String> photoUrls = new java.util.ArrayList<>();
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
                photoUrls.add("/uploads/surrender-dogs/" + uniqueFileName);
            }
        }
        return photoUrls;
    }

    private void deletePhotosFromServer(List<String> photoUrls) {
        if (photoUrls != null && !photoUrls.isEmpty()) {
            System.out.println("Attempting to delete " + photoUrls.size() + " surrender dog photo files...");
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
                        System.out.println("Successfully deleted surrender dog photo file: " + filePath);
                    } else {
                        System.out.println("Surrender dog photo file not found (may already be deleted): " + filePath);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete surrender dog photo file: " + photoUrl + ". Error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error deleting surrender dog photo file: " + photoUrl + ". Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("No surrender dog photos to delete for this request.");
        }
    }

    public SurrenderDogEntity saveSurrenderRequest(SurrenderDogEntity surrenderRequest, List<MultipartFile> photos) throws IOException {
        if (surrenderRequest.getOwnerName() == null || surrenderRequest.getOwnerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name is required");
        }
        if (surrenderRequest.getDogName() == null || surrenderRequest.getDogName().trim().isEmpty()) {
            throw new IllegalArgumentException("Dog name is required");
        }
        if (surrenderRequest.getSurrenderReason() == null || surrenderRequest.getSurrenderReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Surrender reason is required");
        }

        if (photos != null && !photos.isEmpty()) {
            List<String> photoUrls = storePhotos(photos);
            surrenderRequest.setDogPhotoUrls(photoUrls);
        }

        surrenderRequest.setRequestStatus("PENDING");
        surrenderRequest.setSubmissionDate(LocalDateTime.now());
        surrenderRequest.setLastUpdated(LocalDateTime.now());

        return surrenderDogRepository.save(surrenderRequest);
    }

    @Transactional(readOnly = true)
    public List<SurrenderDogEntity> getAll() {
        return surrenderDogRepository.findAll();
    }
    
    public List<SurrenderDogEntity> getByUserId(Long userId) {
        return surrenderDogRepository.findByUserId(userId);
    }

    public Optional<SurrenderDogEntity> getSurrenderRequestById(Long surrenderId) {
        return surrenderDogRepository.findById(surrenderId);
    }

    public SurrenderDogEntity updateSurrenderRequest(Long surrenderId, SurrenderDogEntity updatedRequest, List<MultipartFile> photos) throws IOException {
        return surrenderDogRepository.findById(surrenderId)
                .map(existingRequest -> {
                    existingRequest.setOwnerName(updatedRequest.getOwnerName());
                    existingRequest.setOwnerPhone(updatedRequest.getOwnerPhone());
                    existingRequest.setOwnerEmail(updatedRequest.getOwnerEmail());
                    existingRequest.setOwnerAddress(updatedRequest.getOwnerAddress());

                    existingRequest.setDogName(updatedRequest.getDogName());
                    existingRequest.setDogBreed(updatedRequest.getDogBreed());
                    existingRequest.setDogAge(updatedRequest.getDogAge());
                    existingRequest.setDogGender(updatedRequest.getDogGender());
                    existingRequest.setDogSize(updatedRequest.getDogSize());
                    existingRequest.setDogDescription(updatedRequest.getDogDescription());

                    existingRequest.setVaccinated(updatedRequest.isVaccinated());
                    existingRequest.setNeutered(updatedRequest.isNeutered());
                    existingRequest.setHasMedicalIssues(updatedRequest.isHasMedicalIssues());
                    existingRequest.setMedicalHistory(updatedRequest.getMedicalHistory());

                    existingRequest.setSurrenderReason(updatedRequest.getSurrenderReason());
                    existingRequest.setUrgent(updatedRequest.isUrgent());
                    existingRequest.setPreferredDate(updatedRequest.getPreferredDate());

                    if (photos != null && !photos.isEmpty()) {
                        try {
                            List<String> photoUrls = storePhotos(photos);
                            existingRequest.setDogPhotoUrls(photoUrls);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to store photos during update", e);
                        }
                    }

                    existingRequest.setLastUpdated(LocalDateTime.now());

                    return surrenderDogRepository.save(existingRequest);
                })
                .orElseThrow(() -> new RuntimeException("Surrender request not found with ID: " + surrenderId));
    }

    @Transactional
    public void deleteSurrenderRequest(Long surrenderId) {
        Optional<SurrenderDogEntity> surrenderRequestOptional = surrenderDogRepository.findById(surrenderId);
        if (surrenderRequestOptional.isEmpty()) {
            throw new RuntimeException("Surrender request not found with ID: " + surrenderId);
        }

        SurrenderDogEntity surrenderRequest = surrenderRequestOptional.get();
        // Delete associated photo files from the server
        deletePhotosFromServer(surrenderRequest.getDogPhotoUrls());

        // Clear photo URLs from the entity to break database association
        surrenderRequest.getDogPhotoUrls().clear();

        // Now delete the entity from the database
        surrenderDogRepository.delete(surrenderRequest);
    }

    public List<SurrenderDogEntity> getRequestsByStatus(String status) {
        return surrenderDogRepository.findByRequestStatusOrderBySubmissionDateDesc(status);
    }

    public List<SurrenderDogEntity> getPendingRequestsByPriority() {
        return surrenderDogRepository.findAllPendingRequestsOrderedByUrgency();
    }

    public List<SurrenderDogEntity> getUrgentRequests() {
        return surrenderDogRepository.findByIsUrgentTrueOrderBySubmissionDateAsc();
    }

    public SurrenderDogEntity updateRequestStatus(Long surrenderId, String newStatus, String adminNotes) {
        return surrenderDogRepository.findById(surrenderId)
                .map(request -> {
                    request.setRequestStatus(newStatus);
                    if (adminNotes != null && !adminNotes.trim().isEmpty()) {
                        request.setAdminNotes(adminNotes);
                    }
                    request.setLastUpdated(LocalDateTime.now());
                    return surrenderDogRepository.save(request);
                })
                .orElseThrow(() -> new RuntimeException("Surrender request not found with ID: " + surrenderId));
    }

    public List<SurrenderDogEntity> getUserRequestsByPhone(String ownerPhone) {
        return surrenderDogRepository.findByOwnerPhoneOrderBySubmissionDateDesc(ownerPhone);
    }

    public List<SurrenderDogEntity> getUserRequestsByEmail(String ownerEmail) {
        return surrenderDogRepository.findByOwnerEmailOrderBySubmissionDateDesc(ownerEmail);
    }

    public List<SurrenderDogEntity> getRecentRequests() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return surrenderDogRepository.findRecentRequests(thirtyDaysAgo);
    }

    public long getRequestCountByStatus(String status) {
        return surrenderDogRepository.countByStatus(status);
    }

    public List<SurrenderDogEntity> searchByDogCharacteristics(String breed, String size, String gender) {
        return surrenderDogRepository.findByDogCharacteristics(breed, size, gender);
    }

    public List<SurrenderDogEntity> getRequestsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return surrenderDogRepository.findBySubmissionDateBetweenOrderBySubmissionDateDesc(startDate, endDate);
    }
}
