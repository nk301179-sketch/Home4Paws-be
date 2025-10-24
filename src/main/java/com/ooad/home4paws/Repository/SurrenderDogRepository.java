package com.ooad.home4paws.Repository;

import com.ooad.home4paws.Entity.SurrenderDogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SurrenderDogRepository extends JpaRepository<SurrenderDogEntity, Long> {

    List<SurrenderDogEntity> findByRequestStatusOrderBySubmissionDateDesc(String requestStatus);

    List<SurrenderDogEntity> findByOwnerPhoneOrderBySubmissionDateDesc(String ownerPhone);
    
    List<SurrenderDogEntity> findByUserId(Long userId);

    List<SurrenderDogEntity> findByOwnerEmailOrderBySubmissionDateDesc(String ownerEmail);

    List<SurrenderDogEntity> findByIsUrgentTrueOrderBySubmissionDateAsc();

    @Query("SELECT s FROM SurrenderDogEntity s WHERE s.requestStatus = 'PENDING' ORDER BY s.isUrgent DESC, s.submissionDate ASC")
    List<SurrenderDogEntity> findAllPendingRequestsOrderedByUrgency();

    List<SurrenderDogEntity> findBySubmissionDateBetweenOrderBySubmissionDateDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT COUNT(s) FROM SurrenderDogEntity s WHERE s.requestStatus = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT s FROM SurrenderDogEntity s WHERE s.submissionDate >= :thirtyDaysAgo ORDER BY s.submissionDate DESC")
    List<SurrenderDogEntity> findRecentRequests(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    @Query("SELECT s FROM SurrenderDogEntity s WHERE " +
            "(:breed IS NULL OR LOWER(s.dogBreed) LIKE LOWER(CONCAT('%', :breed, '%'))) AND " +
            "(:size IS NULL OR s.dogSize = :size) AND " +
            "(:gender IS NULL OR s.dogGender = :gender)")
    List<SurrenderDogEntity> findByDogCharacteristics(
            @Param("breed") String breed,
            @Param("size") String size,
            @Param("gender") String gender
    );
}
