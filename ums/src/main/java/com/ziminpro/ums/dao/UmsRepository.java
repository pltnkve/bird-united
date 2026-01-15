package com.ziminpro.ums.dao;

import com.ziminpro.ums.dtos.RefreshToken;
import com.ziminpro.ums.dtos.Roles;
import com.ziminpro.ums.dtos.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UmsRepository {

    Map<UUID, User> findAllUsers();

    Map<String, Roles> findAllRoles();

    User findUserByID(UUID userId);

    UUID createUser(User user);

    int deleteUser(UUID userId);

    Optional<User> findUserByEmail(String email);

    void saveRefreshToken(RefreshToken token);

    Optional<RefreshToken> findRefreshTokenByToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(UUID userId);
}
