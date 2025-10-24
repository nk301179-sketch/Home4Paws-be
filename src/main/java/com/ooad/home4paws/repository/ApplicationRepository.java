package com.ooad.home4paws.repository;

import com.ooad.home4paws.entity.Application;
import com.ooad.home4paws.entity.ApplicationStatus;
import com.ooad.home4paws.entity.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByUserId(Long userId);
    
    List<Application> findByDogId(Long dogId);
    
    List<Application> findByStatus(ApplicationStatus status);
    
    List<Application> findByType(ApplicationType type);
    
    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);
}
