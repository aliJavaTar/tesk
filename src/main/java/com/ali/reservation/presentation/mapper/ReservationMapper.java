package com.ali.reservation.presentation.mapper;

import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;
import com.ali.reservation.presentation.dto.response.ReservationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponse mapTo(AvailableSlotEntity availableSlotEntity);
}