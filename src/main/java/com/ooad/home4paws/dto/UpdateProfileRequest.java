package com.ooad.home4paws.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    // Username included in payload but ignored for update to keep things simple
    private String username;
}
