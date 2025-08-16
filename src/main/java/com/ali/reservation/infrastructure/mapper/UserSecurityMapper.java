package com.ali.reservation.infrastructure.mapper;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.security.UserSecurity;

@Mapper(componentModel = "spring")
public interface UserSecurityMapper {
    UserSecurity toUserSecurity(UserEntity userEntity);
}