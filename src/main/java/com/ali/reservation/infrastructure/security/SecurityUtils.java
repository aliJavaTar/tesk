package com.ali.reservation.infrastructure.security;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserSecurity user)) {
            throw new IllegalStateException("No authenticated user found or principal is not of type UserEntity");
        }
        return user.getId();
    }

}
