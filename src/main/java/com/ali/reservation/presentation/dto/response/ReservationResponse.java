package com.ali.reservation.presentation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long slotId;
    private Long userId;
    private LocalDateTime reservedAt;
}