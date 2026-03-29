package com.scheduler.dto;

import com.scheduler.entity.Interval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interval information")
public class IntervalResponse {
    @Schema(description = "Interval ID")
    private String id;

    @Schema(description = "Owner ID")
    private String ownerId;

    @Schema(description = "Owner username")
    private String ownerName;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Start time")
    private String startTime;

    @Schema(description = "End time")
    private String endTime;

    @Schema(description = "Is booked")
    private boolean isBooked;

    @Schema(description = "Booked by user ID")
    private String bookedBy;

    @Schema(description = "Creation date")
    private String createdAt;

    @Schema(description = "Update date")
    private String updatedAt;

    public static IntervalResponse from(Interval interval) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        return new IntervalResponse(
                interval.getId().toString(),
                interval.getOwner().getId().toString(),
                interval.getOwner().getUsername(),
                interval.getTitle(),
                interval.getStartTime().format(fmt),
                interval.getEndTime().format(fmt),
                interval.getIsBooked(),
                interval.getBookedBy() != null ? interval.getBookedBy().getId().toString() : null,
                interval.getCreatedAt().format(fmt),
                interval.getUpdatedAt().format(fmt)
        );
    }
}
