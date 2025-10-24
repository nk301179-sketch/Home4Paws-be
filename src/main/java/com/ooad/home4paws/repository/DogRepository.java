package com.ooad.home4paws.repository;

import com.ooad.home4paws.entity.Dog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DogRepository extends JpaRepository<Dog, Long> {
    
    List<Dog> findByStatus(com.ooad.home4paws.entity.DogStatus status);
    
    List<Dog> findByIsStray(Boolean isStray);
    
    List<Dog> findByNameContainingIgnoreCase(String name);
    
    List<Dog> findByBreedContainingIgnoreCase(String breed);
}
