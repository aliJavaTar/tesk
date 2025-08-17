package com.ali.reservation.infrastructure.persistence.entity;

import com.ali.reservation.infrastructure.persistence.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@NoArgsConstructor
@Getter
@SoftDelete
@Setter
public class ReservationEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", insertable = false, updatable = false)
    private AvailableSlotEntity slot;

    private ReservationEntity(Long userId, Long slotId, LocalDateTime now, AvailableSlotEntity slot) {
        this.userId = userId;
        this.slotId = slotId;
        this.reservedAt = now;
        this.slot = slot;
    }


    public static ReservationEntity of(Long userId, AvailableSlotEntity slot) {
        return new ReservationEntity(userId, slot.getId(), LocalDateTime.now(), slot);
    }

    @PrePersist
    public void prePersist() {
        if (reservedAt == null) {
            reservedAt = LocalDateTime.now();
        }
    }
}
