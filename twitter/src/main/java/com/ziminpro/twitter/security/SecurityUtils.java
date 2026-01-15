package com.ziminpro.twitter.security;

import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId(ServerWebExchange exchange) {
        String userId = exchange.getAttribute("userId");
        return userId != null ? UUID.fromString(userId) : null;
    }

    public static String getCurrentUserEmail(ServerWebExchange exchange) {
        return exchange.getAttribute("email");
    }

    public static List<String> getCurrentUserRoles(ServerWebExchange exchange) {
        return exchange.getAttribute("roles");
    }

    public static boolean hasRole(ServerWebExchange exchange, String role) {
        List<String> roles = getCurrentUserRoles(exchange);
        return roles != null && roles.contains(role);
    }
}
