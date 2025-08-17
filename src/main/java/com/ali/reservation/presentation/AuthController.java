package com.ali.reservation.presentation;

import com.ali.reservation.presentation.dto.reqeust.LoginRequest;
import com.ali.reservation.presentation.dto.response.AuthResponse;
import com.ali.reservation.presentation.dto.response.RegisterRequest;
import com.ali.reservation.usecase.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse register = authService.register(request);
        return ResponseEntity.ok(register);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse register = authService.login(request);
        return ResponseEntity.ok(register);
    }


}
