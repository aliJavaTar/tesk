package com.ali.reservation.infrastructure.mapper;

import com.ali.reservation.infrastructure.persistence.entity.ReservationEntity;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationResponse toReservationResponse(ReservationEntity entity);
}