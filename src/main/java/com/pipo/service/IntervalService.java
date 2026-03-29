package com.pipo.service;

import com.pipo.dto.*;
import com.pipo.entity.Interval;
import com.pipo.entity.User;
import com.pipo.repository.IntervalRepository;
import com.pipo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntervalService {
    private final IntervalRepository intervalRepository;
    private final UserRepository userRepository;

    @Transactional
    public IntervalResponse create(UUID ownerId, CreateIntervalRequest request) {
        log.info("Creating interval for user: {}", ownerId);

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Interval interval = Interval.builder()
                .owner(owner)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        interval = intervalRepository.save(interval);
        log.info("Interval created: {}", interval.getId());
        return IntervalResponse.from(interval);
    }

    @Transactional(readOnly = true)
    public IntervalResponse getById(UUID id) {
        Interval interval = intervalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interval not found"));
        return IntervalResponse.from(interval);
    }

    @Transactional(readOnly = true)
    public List<IntervalResponse> getMyIntervals(UUID ownerId) {
        return intervalRepository.findByOwnerIdOrderByStartTime(ownerId)
                .stream()
                .map(IntervalResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IntervalResponse> getAvailable() {
        return intervalRepository.findAvailableIntervals()
                .stream()
                .map(IntervalResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public IntervalResponse update(UUID id, UUID ownerId, UpdateIntervalRequest request) {
        log.info("Updating interval: {} by user: {}", id, ownerId);

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Interval interval = intervalRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Interval not found or not owned by you"));

        interval.setTitle(request.getTitle());
        interval.setStartTime(request.getStartTime());
        interval.setEndTime(request.getEndTime());

        interval = intervalRepository.save(interval);
        log.info("Interval updated: {}", interval.getId());
        return IntervalResponse.from(interval);
    }

    @Transactional
    public void delete(UUID id, UUID ownerId) {
        log.info("Deleting interval: {} by user: {}", id, ownerId);
        int deleted = intervalRepository.deleteByIdAndOwnerId(id, ownerId);
        if (deleted == 0) {
            throw new IllegalArgumentException("Interval not found or not owned by you");
        }
        log.info("Interval deleted: {}", id);
    }

    @Transactional
    public IntervalResponse book(UUID id, UUID bookerId) {
        log.info("Booking interval: {} by user: {}", id, bookerId);

        Interval interval = intervalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interval not found"));

        if (interval.getIsBooked()) {
            throw new IllegalArgumentException("Interval already booked");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        interval.setIsBooked(true);
        interval.setBookedBy(booker);
        interval = intervalRepository.save(interval);

        log.info("Interval booked: {} by user: {}", id, bookerId);
        return IntervalResponse.from(interval);
    }

    @Transactional
    public IntervalResponse cancelBooking(UUID id, UUID bookerId) {
        log.info("Cancelling booking for interval: {} by user: {}", id, bookerId);

        Interval interval = intervalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interval not found"));

        if (!interval.getIsBooked() || interval.getBookedBy() == null ||
                !interval.getBookedBy().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Booking not found");
        }

        interval.setIsBooked(false);
        interval.setBookedBy(null);
        interval = intervalRepository.save(interval);

        log.info("Booking cancelled for interval: {}", id);
        return IntervalResponse.from(interval);
    }
}
