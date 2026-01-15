package com.ziminpro.ums.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    private UUID id;
    private UUID userId;
    private String token;
    private long expiresAt;
    private long createdAt;
    private boolean revoked;
}
