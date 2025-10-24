package com.ooad.home4paws.Controller;

import com.ooad.home4paws.Entity.SurrenderDogEntity;
import com.ooad.home4paws.Service.SurrenderDogService;
import com.ooad.home4paws.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/surrender-dogs")
public class SurrenderDogController {

    private final SurrenderDogService surrenderDogService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public SurrenderDogController(SurrenderDogService service, ObjectMapper mapper, UserRepository userRepository) {
        this.surrenderDogService = service;
        this.objectMapper = mapper;
        this.userRepository = userRepository;
    }

    // CREATE (requires authentication)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createSurrenderRequest(
            @RequestPart("surrenderRequest") String surrenderRequestJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        try {
            Map<String, Object> map = objectMapper.readValue(surrenderRequestJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            map.remove("dogPhotoUrl"); // legacy field
            SurrenderDogEntity surrenderDog = objectMapper.convertValue(map, SurrenderDogEntity.class);

            // Get the current authenticated user's ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Set userId on the surrender request
            userRepository.findByUsername(username).ifPresent(user -> {
                surrenderDog.setUserId(user.getId());
            });

            SurrenderDogEntity saved = surrenderDogService.saveSurrenderRequest(surrenderDog, photos);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateSurrenderRequest(
            @PathVariable Long id,
            @RequestPart("surrenderRequest") String surrenderRequestJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        try {
            Map<String, Object> map = objectMapper.readValue(surrenderRequestJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            map.remove("dogPhotoUrl"); // legacy field
            SurrenderDogEntity surrenderDog = objectMapper.convertValue(map, SurrenderDogEntity.class);

            SurrenderDogEntity updated = surrenderDogService.updateSurrenderRequest(id, surrenderDog, photos);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSurrenderRequest(@PathVariable Long id) {
        surrenderDogService.deleteSurrenderRequest(id);
        return ResponseEntity.noContent().build();
    }

    // LIST ALL (public - for adoption page, guests can view)
    @GetMapping
    public ResponseEntity<List<SurrenderDogEntity>> getAll() {
        return ResponseEntity.ok(surrenderDogService.getAll());
    }
    
    // GET current user's surrender requests (requires authentication)
    @GetMapping("/my-requests")
    public ResponseEntity<List<SurrenderDogEntity>> getMyRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(surrenderDogService.getByUserId(user.getId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }
}
