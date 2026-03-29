package com.scheduler.repository;

import com.scheduler.entity.Interval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IntervalRepository extends JpaRepository<Interval, UUID> {
    List<Interval> findByOwnerIdOrderByStartTime(UUID ownerId);

    @Query("SELECT i FROM Interval i WHERE i.isBooked = false ORDER BY i.startTime")
    List<Interval> findAvailableIntervals();

    @Query("SELECT i FROM Interval i WHERE i.bookedBy.id = :bookerId ORDER BY i.startTime")
    List<Interval> findByBookedById(@Param("bookerId") UUID bookerId);

    @Query("SELECT i FROM Interval i WHERE i.id = :id AND i.owner.id = :ownerId")
    Optional<Interval> findByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Modifying
    @Query("DELETE FROM Interval i WHERE i.id = :id AND i.owner.id = :ownerId")
    int deleteByIdAndOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);
}
