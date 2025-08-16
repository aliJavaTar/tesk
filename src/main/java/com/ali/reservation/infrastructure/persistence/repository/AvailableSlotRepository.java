package com.ali.reservation.infrastructure.persistence.repository;


import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlotEntity, Long> {

    @Modifying
    @Query("UPDATE AvailableSlotEntity s SET s.isReserved = true WHERE s.id = :id AND s.isReserved = false")
    int reserveSlot(@Param("id") Long id);
}
