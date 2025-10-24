package com.ooad.home4paws.repository;

import com.ooad.home4paws.entity.ContactMessage;
import com.ooad.home4paws.entity.ContactMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findByUserIdOrderBySubmittedAtDesc(Long userId);
    List<ContactMessage> findByStatusOrderBySubmittedAtDesc(ContactMessageStatus status);
    List<ContactMessage> findAllByOrderBySubmittedAtDesc();
}
