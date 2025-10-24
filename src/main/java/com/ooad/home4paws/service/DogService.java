package com.ooad.home4paws.service;

import com.ooad.home4paws.entity.Dog;
import com.ooad.home4paws.entity.DogStatus;
import com.ooad.home4paws.repository.DogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DogService {
    
    @Autowired
    private DogRepository dogRepository;
    
    public List<Dog> getAllDogs() {
        return dogRepository.findAll();
    }
    
    public Optional<Dog> getDogById(Long id) {
        return dogRepository.findById(id);
    }
    
    public Dog createDog(Dog dog) {
        return dogRepository.save(dog);
    }
    
    public Dog updateDog(Long id, Dog dogDetails) {
        Optional<Dog> optionalDog = dogRepository.findById(id);
        if (optionalDog.isPresent()) {
            Dog dog = optionalDog.get();
            dog.setName(dogDetails.getName());
            dog.setBreed(dogDetails.getBreed());
            dog.setDescription(dogDetails.getDescription());
            dog.setPrice(dogDetails.getPrice());
            dog.setStatus(dogDetails.getStatus());
            dog.setIsStray(dogDetails.getIsStray());
            dog.setImage(dogDetails.getImage());
            return dogRepository.save(dog);
        }
        return null;
    }
    
    public boolean deleteDog(Long id) {
        if (dogRepository.existsById(id)) {
            dogRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<Dog> getDogsByStatus(DogStatus status) {
        return dogRepository.findByStatus(status);
    }
    
    public List<Dog> getStrayDogs() {
        return dogRepository.findByIsStray(true);
    }
    
    public List<Dog> getDogsForSale() {
        return dogRepository.findByIsStray(false);
    }
}
