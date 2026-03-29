package com.scheduler.service;

import com.scheduler.dto.CreateIntervalRequest;
import com.scheduler.dto.IntervalResponse;
import com.scheduler.dto.UpdateIntervalRequest;
import com.scheduler.entity.Interval;
import com.scheduler.entity.User;
import com.scheduler.repository.IntervalRepository;
import com.scheduler.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntervalServiceTest {
    @Mock
    private IntervalRepository intervalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IntervalService intervalService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@test.com")
                .passwordHash("hashedpassword")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateInterval() {
        CreateIntervalRequest request = new CreateIntervalRequest(
                "Test Interval",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2)
        );

        Interval savedInterval = Interval.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(intervalRepository.save(any(Interval.class))).thenReturn(savedInterval);

        IntervalResponse response = intervalService.create(userId, request);

        assertNotNull(response);
        assertEquals(request.getTitle(), response.getTitle());
        assertFalse(response.isBooked());
        verify(intervalRepository).save(any(Interval.class));
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        CreateIntervalRequest request = new CreateIntervalRequest(
                "Test Interval",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(1)
        );

        assertThrows(IllegalArgumentException.class,
                () -> intervalService.create(userId, request));
    }

    @Test
    void shouldGetAvailableIntervals() {
        Interval interval = Interval.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .title("Available Interval")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isBooked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(intervalRepository.findAvailableIntervals()).thenReturn(List.of(interval));

        List<IntervalResponse> result = intervalService.getAvailable();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isBooked());
    }

    @Test
    void shouldBookInterval() {
        UUID intervalId = UUID.randomUUID();
        UUID bookerId = UUID.randomUUID();
        User booker = User.builder()
                .id(bookerId)
                .username("booker")
                .email("booker@test.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Interval interval = Interval.builder()
                .id(intervalId)
                .owner(testUser)
                .title("Test Interval")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isBooked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Interval bookedInterval = Interval.builder()
                .id(intervalId)
                .owner(testUser)
                .title("Test Interval")
                .startTime(interval.getStartTime())
                .endTime(interval.getEndTime())
                .isBooked(true)
                .bookedBy(booker)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(intervalRepository.findById(intervalId)).thenReturn(Optional.of(interval));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(intervalRepository.save(any(Interval.class))).thenReturn(bookedInterval);

        IntervalResponse response = intervalService.book(intervalId, bookerId);

        assertTrue(response.isBooked());
        assertEquals(bookerId.toString(), response.getBookedBy());
    }

    @Test
    void shouldRejectBookingAlreadyBooked() {
        UUID intervalId = UUID.randomUUID();
        Interval bookedInterval = Interval.builder()
                .id(intervalId)
                .owner(testUser)
                .title("Test Interval")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isBooked(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(intervalRepository.findById(intervalId)).thenReturn(Optional.of(bookedInterval));

        assertThrows(IllegalArgumentException.class,
                () -> intervalService.book(intervalId, UUID.randomUUID()));
    }

    @Test
    void shouldUpdateInterval() {
        UUID intervalId = UUID.randomUUID();
        UpdateIntervalRequest request = new UpdateIntervalRequest(
                "Updated Title",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(3)
        );

        Interval existingInterval = Interval.builder()
                .id(intervalId)
                .owner(testUser)
                .title("Original Title")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .isBooked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Interval updatedInterval = Interval.builder()
                .id(intervalId)
                .owner(testUser)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .createdAt(existingInterval.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(intervalRepository.findByIdAndOwnerId(intervalId, userId))
                .thenReturn(Optional.of(existingInterval));
        when(intervalRepository.save(any(Interval.class))).thenReturn(updatedInterval);

        IntervalResponse response = intervalService.update(intervalId, userId, request);

        assertEquals("Updated Title", response.getTitle());
    }

    @Test
    void shouldDeleteInterval() {
        UUID intervalId = UUID.randomUUID();
        when(intervalRepository.deleteByIdAndOwnerId(intervalId, userId)).thenReturn(1);

        assertDoesNotThrow(() -> intervalService.delete(intervalId, userId));
        verify(intervalRepository).deleteByIdAndOwnerId(intervalId, userId);
    }

    @Test
    void shouldRejectDeleteNonExistent() {
        UUID intervalId = UUID.randomUUID();
        when(intervalRepository.deleteByIdAndOwnerId(intervalId, userId)).thenReturn(0);

        assertThrows(IllegalArgumentException.class,
                () -> intervalService.delete(intervalId, userId));
    }
}
