package com.ali.reservation.presentation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}