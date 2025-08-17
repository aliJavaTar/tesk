package com.ali.reservation.usecase;

import com.ali.reservation.infrastructure.persistence.entity.UserEntity;
import com.ali.reservation.infrastructure.persistence.repository.UserRepository;
import com.ali.reservation.infrastructure.security.JwtService;
import com.ali.reservation.infrastructure.security.UserSecurity;

import com.ali.reservation.presentation.dto.reqeust.LoginRequest;
import com.ali.reservation.presentation.dto.response.AuthResponse;
import com.ali.reservation.presentation.dto.response.RegisterRequest;
import com.ali.reservation.presentation.exption.ApplicationException;
import com.ali.reservation.presentation.mapper.UserSecurityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ali.reservation.presentation.exption.ErrorType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserSecurityMapper mapper;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with username: {}", request.getUsername());

        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new ApplicationException(DUPLICATE_ERROR, "Username or email already exists");
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        String token = jwtService.generateToken(mapper.toUserSecurity(savedUser));

        return buildAuthResponse(token, savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user with username: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserSecurity userSecurity = (UserSecurity) authentication.getPrincipal();
            String token = jwtService.generateToken(userSecurity);

            log.info("User logged in successfully: {}", request.getUsername());

            UserEntity userEntity = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ApplicationException(USER_NOT_FOUND,"User not found"));

            return buildAuthResponse(token, userEntity);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            throw new ApplicationException(INVALID_CREDENTIALS);
        }
    }

    private AuthResponse buildAuthResponse(String token, UserEntity user) {
        var response = new AuthResponse();
        response.setToken(token);

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());

        response.setUser(userInfo);
        return response;
    }
}