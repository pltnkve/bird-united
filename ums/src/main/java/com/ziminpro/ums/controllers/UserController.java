package com.ziminpro.ums.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.ums.dao.UmsRepository;
import com.ziminpro.ums.dtos.Constants;
import com.ziminpro.ums.dtos.User;
import com.ziminpro.ums.security.RequireRoles;
import com.ziminpro.ums.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    @Autowired
    private UmsRepository umsRepository;

    @RequireRoles({"ADMIN", "SUBSCRIBER", "PRODUCER"})
    @RequestMapping(method = RequestMethod.GET, path = "/users")
    public Mono<ResponseEntity<Map<String, Object>>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        Map<UUID, User> users = umsRepository.findAllUsers();
        if (users == null) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Users have not been retrieved");
            response.put(Constants.DATA, new HashMap<>());
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "List of Users has been requested successfully");
            response.put(Constants.DATA, new ArrayList<>(users.values()));
        }
        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    @RequireRoles({"ADMIN", "SUBSCRIBER", "PRODUCER"})
    @GetMapping("/users/me")
    public Mono<ResponseEntity<Map<String, Object>>> getCurrentUser(ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        UUID userId = SecurityUtils.getCurrentUserId(exchange);

        User user = umsRepository.findUserByID(userId);
        if (user.getId() == null) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "User not found");
            response.put(Constants.DATA, null);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "User retrieved");
            response.put(Constants.DATA, user);
        }

        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .body(response));
    }

    @RequireRoles({"ADMIN", "SUBSCRIBER", "PRODUCER"})
    @RequestMapping(method = RequestMethod.GET, path = "/users/user/{user-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getUser(
            @PathVariable(value = "user-id", required = true) String userId) {
        Map<String, Object> response = new HashMap<>();
        User user = umsRepository.findUserByID(UUID.fromString(userId));
        if (user.getId() == null) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "User have not been found");
            response.put(Constants.DATA, new User());
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "User has been retrieved successfully");
            response.put(Constants.DATA, user);
        }
        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    @RequireRoles({"ADMIN"})
    @RequestMapping(method = RequestMethod.POST, path = "/users/user", consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        UUID userId = umsRepository.createUser(user);
        if (userId == null) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "User has not been created");
            response.put(Constants.DATA, "Check email for duplicates first");
        } else {
            response.put(Constants.CODE, "201");
            response.put(Constants.MESSAGE, "User created");
            response.put(Constants.DATA, userId.toString());
        }
        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    @RequireRoles({"ADMIN"})
    @RequestMapping(method = RequestMethod.DELETE, path = "/users/user/{user-id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteUser(
            @PathVariable(value = "user-id", required = true) String userId) {
        Map<String, Object> response = new HashMap<>();
        int result = umsRepository.deleteUser(UUID.fromString(userId));
        if (result != 1) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Error happened while deleting user");
            response.put(Constants.DATA, userId);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "User deleted");
            response.put(Constants.DATA, userId);
        }
        return Mono.just(ResponseEntity.ok()
                .header(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }
}
