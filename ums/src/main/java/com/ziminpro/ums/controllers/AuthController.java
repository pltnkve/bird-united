package com.ziminpro.ums.controllers;

import com.ziminpro.ums.dtos.*;
import com.ziminpro.ums.security.AuthService;
import com.ziminpro.ums.security.PublicEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PublicEndpoint
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            try {
                AuthResponse authResponse = authService.login(request.getEmail(), request.getPassword());
                response.put(Constants.CODE, "200");
                response.put(Constants.MESSAGE, "Login successful");
                response.put(Constants.DATA, authResponse);
            } catch (Exception e) {
                response.put(Constants.CODE, "401");
                response.put(Constants.MESSAGE, e.getMessage());
                response.put(Constants.DATA, null);
            }
            return ResponseEntity.ok()
                    .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response);
        });
    }

    @PublicEndpoint
    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refresh(@RequestBody RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            try {
                AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
                response.put(Constants.CODE, "200");
                response.put(Constants.MESSAGE, "Token refreshed");
                response.put(Constants.DATA, authResponse);
            } catch (Exception e) {
                response.put(Constants.CODE, "401");
                response.put(Constants.MESSAGE, e.getMessage());
                response.put(Constants.DATA, null);
            }
            return ResponseEntity.ok()
                    .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response);
        });
    }

    @PublicEndpoint
    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, Object>>> logout(@RequestBody RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
            Map<String, Object> response = new HashMap<>();
            try {
                authService.logout(request.getRefreshToken());
                response.put(Constants.CODE, "200");
                response.put(Constants.MESSAGE, "Logged out successfully");
                response.put(Constants.DATA, null);
            } catch (Exception e) {
                response.put(Constants.CODE, "500");
                response.put(Constants.MESSAGE, e.getMessage());
                response.put(Constants.DATA, null);
            }
            return ResponseEntity.ok()
                    .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                    .body(response);
        });
    }
}
