package com.ali.reservation.infrastructure.persistence.repository;


import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlotEntity, Long> {


    @Query("SELECT s FROM AvailableSlotEntity s WHERE s.isReserved = false " +
            "AND s.startTime >= :fromTime ORDER BY s.startTime")
    Page<AvailableSlotEntity> findAvailableSlots(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);

    @Query("SELECT s FROM AvailableSlotEntity s WHERE s.isReserved = false " +
            "AND s.startTime = :fromTime")
    Optional<AvailableSlotEntity> findByStartTime(@Param("fromTime") LocalDateTime fromTime);

}
