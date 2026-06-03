package org.modmed.patientservice.dto;

import jakarta.validation.constraints.*;

public record PatientRequestDTO(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be greater than 0")
        @Max(value = 150, message = "Age must be less than 150")
        Integer age,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
        String mobile,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email
) {}
