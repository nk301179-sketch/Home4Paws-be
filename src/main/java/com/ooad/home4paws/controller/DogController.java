package com.ooad.home4paws.controller;

import com.ooad.home4paws.entity.Dog;
import com.ooad.home4paws.entity.DogStatus;
import com.ooad.home4paws.service.DogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dogs")
@Tag(name = "Dogs", description = "Public dog endpoints for viewing dogs")
public class DogController {

    @Autowired
    private DogService dogService;

    @Operation(summary = "Get all dogs", description = "Get list of all dogs available for adoption and purchase")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dogs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<Dog>> getAllDogs() {
        List<Dog> dogs = dogService.getAllDogs();
        return ResponseEntity.ok(dogs);
    }

    @Operation(summary = "Get dog by ID", description = "Get a specific dog by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dog retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Dog not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getDogById(@PathVariable Long id) {
        Optional<Dog> dog = dogService.getDogById(id);
        if (dog.isPresent()) {
            return ResponseEntity.ok(dog.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get dogs for adoption", description = "Get list of dogs available for adoption (stray dogs)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Adoption dogs retrieved successfully")
    })
    @GetMapping("/adopt")
    public ResponseEntity<List<Dog>> getDogsForAdoption() {
        List<Dog> dogs = dogService.getStrayDogs();
        return ResponseEntity.ok(dogs);
    }

    @Operation(summary = "Get dogs for sale", description = "Get list of dogs available for purchase")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dogs for sale retrieved successfully")
    })
    @GetMapping("/buy")
    public ResponseEntity<List<Dog>> getDogsForSale() {
        List<Dog> dogs = dogService.getDogsForSale();
        return ResponseEntity.ok(dogs);
    }

    @Operation(summary = "Get dogs by status", description = "Get dogs filtered by status (AVAILABLE, PENDING, ADOPTED, SOLD)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dogs retrieved successfully")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Dog>> getDogsByStatus(@PathVariable DogStatus status) {
        List<Dog> dogs = dogService.getDogsByStatus(status);
        return ResponseEntity.ok(dogs);
    }
}
