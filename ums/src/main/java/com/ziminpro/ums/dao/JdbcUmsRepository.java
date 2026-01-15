package com.ziminpro.ums.dao;

import java.time.Instant;
import java.util.*;

import com.ziminpro.ums.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUmsRepository implements UmsRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<UUID, User> findAllUsers() {
        Map<UUID, User> users = new HashMap<>();

        List<Object> oUsers = jdbcTemplate.query(Constants.GET_ALL_USERS,
                (rs, rowNum) -> new User(DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")), rs.getString("users.name"),
                        rs.getString("users.email"), rs.getString("users.password"), rs.getInt("users.created"),
                        List.of(new Roles(DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"), rs.getString("roles.description"))),
                        new LastSession(rs.getInt("last_visit.in"), rs.getInt("last_visit.out"))));

        for (Object oUser : oUsers) {
            if (!users.containsKey(((User) oUser).getId())) {
                User user = new User();
                user.setId(((User) oUser).getId());
                user.setName(((User) oUser).getName());
                user.setEmail(((User) oUser).getEmail());
                user.setPassword(((User) oUser).getPassword());
                user.setCreated(((User) oUser).getCreated());
                user.setLastSession(((User) oUser).getLastSession());
                users.put(((User) oUser).getId(), user);
            }
            users.get(((User) oUser).getId()).addRole(((User) oUser).getRoles().getFirst());
        }
        return users;
    }

    @Override
    public User findUserByID(UUID userId) {
        User user = new User();
        List<Object> users = jdbcTemplate.query(Constants.GET_USER_BY_ID_FULL,
                (rs, rowNum) -> new User(DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")), rs.getString("users.name"),
                        rs.getString("users.email"), rs.getString("users.password"), rs.getInt("users.created"),
                        List.of(new Roles(DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"), rs.getString("roles.description"))),
                        new LastSession(rs.getInt("last_visit.in"), rs.getInt("last_visit.out"))),
                userId.toString());
        for (Object oUser : users) {
            if (user.getId() == null) {
                user.setId(((User) oUser).getId());
                user.setName(((User) oUser).getName());
                user.setEmail(((User) oUser).getEmail());
                user.setPassword(((User) oUser).getPassword());
                user.setCreated(((User) oUser).getCreated());
                user.setLastSession(((User) oUser).getLastSession());
            }
            user.addRole(((User) oUser).getRoles().getFirst());
        }
        return user;
    }

    @Override
    public UUID createUser(User user) {
        long timestamp = Instant.now().getEpochSecond();
        Map<String, Roles> roles = this.findAllRoles();
        UUID userId = UUID.randomUUID();

        try {
            jdbcTemplate.update(Constants.CREATE_USER, userId.toString(), user.getName(), user.getEmail(),
                    user.getPassword(), timestamp, null);
            for (Roles role : user.getRoles()) {
                jdbcTemplate.update(Constants.ASSIGN_ROLE, userId.toString(),
                        roles.get(role.getRole()).getRoleId().toString());
            }
        } catch (Exception e) {
            return null;
        }

        return userId;
    }

    @Override
    public int deleteUser(UUID userId) {
        return jdbcTemplate.update(Constants.DELETE_USER, userId.toString());
    }

    @Override
    public Map<String, Roles> findAllRoles() {
        Map<String, Roles> roles = new HashMap<>();
        jdbcTemplate.query(Constants.GET_ALL_ROLES, rs -> {
            Roles role = new Roles(DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")), rs.getString("roles.name"),
                    rs.getString("roles.description"));
            roles.put(rs.getString("roles.name"), role);
        });
        return roles;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        User user = new User();
        List<Object> users = jdbcTemplate.query(Constants.FIND_USER_BY_EMAIL,
                (rs, rowNum) -> new User(
                        DaoHelper.bytesArrayToUuid(rs.getBytes("users.id")),
                        rs.getString("users.name"),
                        rs.getString("users.email"),
                        rs.getString("users.password"),
                        rs.getInt("users.created"),
                        List.of(new Roles(
                                DaoHelper.bytesArrayToUuid(rs.getBytes("roles.id")),
                                rs.getString("roles.name"),
                                rs.getString("roles.description"))),
                        null),
                email);

        for (Object oUser : users) {
            if (user.getId() == null) {
                user.setId(((User) oUser).getId());
                user.setName(((User) oUser).getName());
                user.setEmail(((User) oUser).getEmail());
                user.setPassword(((User) oUser).getPassword());
                user.setCreated(((User) oUser).getCreated());
            }
            user.addRole(((User) oUser).getRoles().getFirst());
        }

        return user.getId() != null ? Optional.of(user) : Optional.empty();
    }

    @Override
    public void saveRefreshToken(RefreshToken token) {
        jdbcTemplate.update(Constants.SAVE_REFRESH_TOKEN,
                token.getId().toString(),
                token.getUserId().toString(),
                token.getToken(),
                token.getExpiresAt(),
                token.getCreatedAt(),
                token.isRevoked());
    }

    @Override
    public Optional<RefreshToken> findRefreshTokenByToken(String token) {
        try {
            RefreshToken refreshToken = jdbcTemplate.queryForObject(Constants.FIND_REFRESH_TOKEN,
                    (rs, rowNum) -> new RefreshToken(
                            DaoHelper.bytesArrayToUuid(rs.getBytes("id")),
                            DaoHelper.bytesArrayToUuid(rs.getBytes("user_id")),
                            rs.getString("token"),
                            rs.getLong("expires_at"),
                            rs.getLong("created_at"),
                            rs.getBoolean("revoked")),
                    token);
            return Optional.ofNullable(refreshToken);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void revokeRefreshToken(String token) {
        jdbcTemplate.update(Constants.REVOKE_TOKEN, token);
    }

    @Override
    public void revokeAllUserTokens(UUID userId) {
        jdbcTemplate.update(Constants.REVOKE_USER_TOKENS, userId.toString());
    }
}
