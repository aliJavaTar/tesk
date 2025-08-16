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

    @Modifying
    @Query("UPDATE AvailableSlotEntity s SET s.isReserved = true " +
            "WHERE s.id = :id AND s.isReserved = false AND s.version = :version")
    int reserveSlotOptimistic(@Param("id") Long id, @Param("version") Long version);

    @Query("SELECT s FROM AvailableSlotEntity s WHERE s.isReserved = false " +
            "AND s.startTime >= :fromTime ORDER BY s.startTime")
    Page<AvailableSlotEntity> findAvailableSlots(@Param("fromTime") LocalDateTime fromTime, Pageable pageable);

    Optional<AvailableSlotEntity> findByIdAndIsReservedFalse(Long id);
}
