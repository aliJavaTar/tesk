package com.ali.reservation.infrastructure.mapper;



import com.ali.reservation.infrastructure.persistence.entity.AvailableSlotEntity;


@Mapper(componentModel = "spring")
public interface SlotMapper {
    SlotResponse toSlotResponse(AvailableSlotEntity entity);
}
