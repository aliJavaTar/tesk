package com.ali.reservation.presentation.mapper;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.security.UserSecurity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSecurityMapper {
    UserSecurity toUserSecurity(UserEntity userEntity);
}