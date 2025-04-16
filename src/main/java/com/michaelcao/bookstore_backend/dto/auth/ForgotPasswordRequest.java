// src/main/java/com/michaelcao/bookstore_backend/dto/auth/ForgotPasswordRequest.java
package com.michaelcao.bookstore_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
}