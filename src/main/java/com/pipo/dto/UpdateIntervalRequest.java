package com.pipo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update interval request")
public class UpdateIntervalRequest {
    @NotBlank
    @Schema(description = "Interval title", example = "Updated Meeting")
    private String title;

    @NotNull
    @Schema(description = "Start time", example = "2025-06-01T10:00:00")
    private LocalDateTime startTime;

    @NotNull
    @Schema(description = "End time", example = "2025-06-01T11:00:00")
    private LocalDateTime endTime;
}
