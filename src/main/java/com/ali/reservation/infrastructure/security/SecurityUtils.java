package com.ali.reservation.infrastructure.security;

import com.ali.reservation.presentation.exption.ApplicationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.ali.reservation.presentation.exption.ErrorType.NO_AUTH_FOUND;

public final class SecurityUtils {

    private static final String NO_AUTHENTICATED = "No authenticated user found or principal is not of type UserEntity";

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserSecurity user)) {
            throw new ApplicationException(NO_AUTH_FOUND, NO_AUTHENTICATED);
        }
        return user.getId();
    }

}
