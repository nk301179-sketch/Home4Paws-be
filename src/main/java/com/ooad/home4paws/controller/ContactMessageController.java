package com.ooad.home4paws.controller;

import com.ooad.home4paws.entity.ContactMessage;
import com.ooad.home4paws.entity.ContactMessageStatus;
import com.ooad.home4paws.service.ContactMessageService;
import com.ooad.home4paws.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/contact-messages")
@CrossOrigin(origins = "*")
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;
    
    @Autowired
    private UserService userService;

    @Operation(summary = "Submit a contact message", description = "Submit a new contact message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<?> submitContactMessage(@RequestBody ContactMessage contactMessage) {
        try {
            // Get user ID from authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                try {
                    String username = authentication.getName();
                    // Get user ID by username
                    var user = userService.findByUsername(username);
                    if (user.isPresent()) {
                        contactMessage.setUserId(user.get().getId());
                    } else {
                        contactMessage.setUserId(null);
                    }
                } catch (Exception e) {
                    contactMessage.setUserId(null);
                }
            } else {
                contactMessage.setUserId(null); // Guest user
            }

            ContactMessage savedMessage = contactMessageService.createContactMessage(contactMessage);
            return ResponseEntity.ok(Map.of(
                "message", "Contact message submitted successfully",
                "id", savedMessage.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to submit contact message: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Get user's contact messages", description = "Get all contact messages submitted by the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/my-messages")
    public ResponseEntity<?> getMyContactMessages() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
                ));
            }

            String username = authentication.getName();
            var user = userService.findByUsername(username);
            if (!user.isPresent()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not found"
                ));
            }
            Long userId = user.get().getId();
            List<ContactMessage> messages = contactMessageService.getContactMessagesByUserId(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required"
            ));
        }
    }

    @Operation(summary = "Get contact message by ID", description = "Get a specific contact message by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactMessage(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
                ));
            }

            String username = authentication.getName();
            var user = userService.findByUsername(username);
            if (!user.isPresent()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not found"
                ));
            }
            Long userId = user.get().getId();
            Optional<ContactMessage> message = contactMessageService.getContactMessageById(id);
            
            if (message.isPresent()) {
                // Check if user owns this message or is admin
                if (message.get().getUserId() != null && message.get().getUserId().equals(userId)) {
                    return ResponseEntity.ok(message.get());
                } else {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "Access denied"
                    ));
                }
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required"
            ));
        }
    }

    @Operation(summary = "Update contact message", description = "Update a contact message (user can only update their own messages)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message updated successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContactMessage(@PathVariable Long id, @RequestBody ContactMessage updatedMessage) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
                ));
            }

            String username = authentication.getName();
            var user = userService.findByUsername(username);
            if (!user.isPresent()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not found"
                ));
            }
            Long userId = user.get().getId();
            Optional<ContactMessage> existingMessage = contactMessageService.getContactMessageById(id);
            
            if (existingMessage.isPresent()) {
                if (existingMessage.get().getUserId() != null && existingMessage.get().getUserId().equals(userId)) {
                    ContactMessage updated = contactMessageService.updateContactMessage(id, updatedMessage);
                    if (updated != null) {
                        return ResponseEntity.ok(updated);
                    } else {
                        return ResponseEntity.status(404).body(Map.of(
                            "error", "Message not found"
                        ));
                    }
                } else {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "Access denied"
                    ));
                }
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required"
            ));
        }
    }

    @Operation(summary = "Delete contact message", description = "Delete a contact message (user can only delete their own messages)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Message not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContactMessage(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
                ));
            }

            String username = authentication.getName();
            var user = userService.findByUsername(username);
            if (!user.isPresent()) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "User not found"
                ));
            }
            Long userId = user.get().getId();
            Optional<ContactMessage> existingMessage = contactMessageService.getContactMessageById(id);
            
            if (existingMessage.isPresent()) {
                if (existingMessage.get().getUserId() != null && existingMessage.get().getUserId().equals(userId)) {
                    boolean deleted = contactMessageService.deleteContactMessage(id);
                    if (deleted) {
                        return ResponseEntity.ok(Map.of(
                            "message", "Contact message deleted successfully"
                        ));
                    } else {
                        return ResponseEntity.status(404).body(Map.of(
                            "error", "Message not found"
                        ));
                    }
                } else {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "Access denied"
                    ));
                }
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Message not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication required"
            ));
        }
    }
}
