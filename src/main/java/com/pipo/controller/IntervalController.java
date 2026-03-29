package com.pipo.controller;

import com.pipo.dto.*;
import com.pipo.service.IntervalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/intervals")
@RequiredArgsConstructor
@Tag(name = "Intervals", description = "Time interval management operations")
public class IntervalController {
    private final IntervalService intervalService;

    @PostMapping
    @Operation(summary = "Create a new interval")
    public ResponseEntity<?> create(Authentication authentication,
                                    @Valid @RequestBody CreateIntervalRequest request) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());
            IntervalResponse response = intervalService.create(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get interval by ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            IntervalResponse response = intervalService.getById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Interval not found"));
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Get my intervals")
    public ResponseEntity<List<IntervalResponse>> getMyIntervals(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        List<IntervalResponse> response = intervalService.getMyIntervals(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available intervals")
    public ResponseEntity<List<IntervalResponse>> getAvailable() {
        List<IntervalResponse> response = intervalService.getAvailable();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an interval")
    public ResponseEntity<?> update(Authentication authentication,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody UpdateIntervalRequest request) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());
            IntervalResponse response = intervalService.update(id, userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an interval")
    public ResponseEntity<?> delete(Authentication authentication, @PathVariable UUID id) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());
            intervalService.delete(id, userId);
            return ResponseEntity.ok(new MessageResponse("Interval deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/book")
    @Operation(summary = "Book an interval")
    public ResponseEntity<?> book(Authentication authentication, @PathVariable UUID id) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());
            IntervalResponse response = intervalService.book(id, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/book")
    @Operation(summary = "Cancel interval booking")
    public ResponseEntity<?> cancelBooking(Authentication authentication, @PathVariable UUID id) {
        try {
            UUID userId = UUID.fromString(authentication.getPrincipal().toString());
            IntervalResponse response = intervalService.cancelBooking(id, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
