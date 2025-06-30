package com.example.socialnetwork.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    @Size(max = 255, message = "Full name must be less than 255 characters")
    private String fullName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 255, message = "Occupation must be less than 255 characters")
    private String occupation;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;
}