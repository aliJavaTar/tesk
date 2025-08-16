package com.ali.reservation.infrastructure.persistence.entity;

import com.ali.reservation.infrastructure.persistence.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "available_slots")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AvailableSlotEntity extends BaseEntity {

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved;
}
