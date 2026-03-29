package com.pipo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Registration request")
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 255)
    @Schema(description = "Username", example = "john_doe")
    private String username;

    @NotBlank
    @Email
    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(description = "Password", example = "password123")
    private String password;
}
