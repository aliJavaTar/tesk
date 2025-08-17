package com.ali.reservation.infrastructure.security;

import com.ali.reservation.presentation.mapper.UserSecurityMapper;
import com.ali.reservation.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final UserSecurityMapper userSecurityMapper;

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        return userRepository.findByUsername(username)
                .map(userSecurityMapper::toUserSecurity)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}