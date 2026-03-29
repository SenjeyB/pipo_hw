package com.scheduler.dto;

import com.scheduler.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information")
public class UserResponse {
    @Schema(description = "User ID")
    private String id;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Creation date")
    private String createdAt;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}
