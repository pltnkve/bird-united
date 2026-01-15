package com.ziminpro.ums.security;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.AuthResponse;
import com.ziminpro.ums.dtos.RefreshToken;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UmsRepository repository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse login(String email, String password) {
        User user = repository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return generateTokens(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        UUID userId = jwtService.getUserIdFromToken(refreshToken);

        RefreshToken storedToken = repository.findRefreshTokenByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt() < Instant.now().toEpochMilli()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        repository.revokeRefreshToken(refreshToken);

        User user = repository.findUserByID(userId);
        return generateTokens(user);
    }

    public void logout(String refreshToken) {
        repository.revokeRefreshToken(refreshToken);
    }

    private AuthResponse generateTokens(User user) {
        var roles = user.getRoles().stream()
                .map(Roles::getRole)
                .collect(Collectors.toList());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshTokenStr = jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setCreatedAt(Instant.now().toEpochMilli());
        refreshToken.setExpiresAt(Instant.now().toEpochMilli() + jwtService.getRefreshTokenExpiration());
        refreshToken.setRevoked(false);

        repository.saveRefreshToken(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenStr,
                "Bearer",
                jwtService.getRefreshTokenExpiration() / 1000
        );
    }
}
