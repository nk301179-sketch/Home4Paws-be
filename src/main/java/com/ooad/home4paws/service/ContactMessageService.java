package com.ooad.home4paws.service;

import com.ooad.home4paws.entity.ContactMessage;
import com.ooad.home4paws.entity.ContactMessageStatus;
import com.ooad.home4paws.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public ContactMessage createContactMessage(ContactMessage contactMessage) {
        return contactMessageRepository.save(contactMessage);
    }

    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAllByOrderBySubmittedAtDesc();
    }

    public List<ContactMessage> getContactMessagesByUserId(Long userId) {
        return contactMessageRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public List<ContactMessage> getContactMessagesByStatus(ContactMessageStatus status) {
        return contactMessageRepository.findByStatusOrderBySubmittedAtDesc(status);
    }

    public Optional<ContactMessage> getContactMessageById(Long id) {
        return contactMessageRepository.findById(id);
    }

    public ContactMessage updateContactMessage(Long id, ContactMessage updatedMessage) {
        Optional<ContactMessage> existingMessage = contactMessageRepository.findById(id);
        if (existingMessage.isPresent()) {
            ContactMessage message = existingMessage.get();
            message.setName(updatedMessage.getName());
            message.setEmail(updatedMessage.getEmail());
            message.setMessage(updatedMessage.getMessage());
            return contactMessageRepository.save(message);
        }
        return null;
    }

    public ContactMessage respondToContactMessage(Long id, String adminResponse) {
        Optional<ContactMessage> existingMessage = contactMessageRepository.findById(id);
        if (existingMessage.isPresent()) {
            ContactMessage message = existingMessage.get();
            message.setAdminResponse(adminResponse);
            message.setStatus(ContactMessageStatus.RESPONDED);
            message.setRespondedAt(LocalDateTime.now());
            return contactMessageRepository.save(message);
        }
        return null;
    }

    public ContactMessage updateContactMessageStatus(Long id, ContactMessageStatus status) {
        Optional<ContactMessage> existingMessage = contactMessageRepository.findById(id);
        if (existingMessage.isPresent()) {
            ContactMessage message = existingMessage.get();
            message.setStatus(status);
            if (status == ContactMessageStatus.CLOSED) {
                message.setRespondedAt(LocalDateTime.now());
            }
            return contactMessageRepository.save(message);
        }
        return null;
    }

    public boolean deleteContactMessage(Long id) {
        if (contactMessageRepository.existsById(id)) {
            contactMessageRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
