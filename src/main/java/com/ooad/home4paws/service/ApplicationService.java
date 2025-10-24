package com.ooad.home4paws.service;

import com.ooad.home4paws.entity.Application;
import com.ooad.home4paws.entity.ApplicationStatus;
import com.ooad.home4paws.entity.ApplicationType;
import com.ooad.home4paws.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
    
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }
    
    public Application createApplication(Application application) {
        return applicationRepository.save(application);
    }
    
    public Application updateApplication(Long id, Application applicationDetails) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);
        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();
            application.setStatus(applicationDetails.getStatus());
            application.setAdminNotes(applicationDetails.getAdminNotes());
            if (applicationDetails.getStatus() != ApplicationStatus.PENDING) {
                application.setProcessedAt(java.time.LocalDateTime.now());
            }
            return applicationRepository.save(application);
        }
        return null;
    }
    
    public boolean deleteApplication(Long id) {
        if (applicationRepository.existsById(id)) {
            applicationRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Application> getApplicationsByUserId(Long userId) {
        return applicationRepository.findByUserId(userId);
    }
    
    public List<Application> getApplicationsByDogId(Long dogId) {
        return applicationRepository.findByDogId(dogId);
    }
    
    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }
    
    public List<Application> getApplicationsByType(ApplicationType type) {
        return applicationRepository.findByType(type);
    }
    
    public List<Application> getUserApplicationsByStatus(Long userId, ApplicationStatus status) {
        return applicationRepository.findByUserIdAndStatus(userId, status);
    }
}
