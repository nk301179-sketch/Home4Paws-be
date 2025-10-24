package com.ooad.home4paws.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dogs")
public class Dog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Dog name is required")
    @Column(nullable = false)
    private String name;
    
    @Column
    private String breed;
    
    @Column
    private String description;
    
    @PositiveOrZero(message = "Price must be positive or zero")
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DogStatus status = DogStatus.AVAILABLE;
    
    @Column(name = "is_stray")
    private Boolean isStray = false;
    
    @Column
    private String image;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Dog() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Dog(String name, String breed, String description, BigDecimal price, DogStatus status, Boolean isStray, String image) {
        this();
        this.name = name;
        this.breed = breed;
        this.description = description;
        this.price = price;
        this.status = status;
        this.isStray = isStray;
        this.image = image;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBreed() {
        return breed;
    }
    
    public void setBreed(String breed) {
        this.breed = breed;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public DogStatus getStatus() {
        return status;
    }
    
    public void setStatus(DogStatus status) {
        this.status = status;
    }
    
    public Boolean getIsStray() {
        return isStray;
    }
    
    public void setIsStray(Boolean isStray) {
        this.isStray = isStray;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
