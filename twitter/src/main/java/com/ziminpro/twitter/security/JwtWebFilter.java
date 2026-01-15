package com.ziminpro.twitter.security;

import io.jsonwebtoken.Claims;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Component
public class JwtWebFilter implements WebFilter {

    private final JwtService jwtService;
    private final ApplicationContext applicationContext;

    public JwtWebFilter(JwtService jwtService, ApplicationContext applicationContext) {
        this.jwtService = jwtService;
        this.applicationContext = applicationContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // КРИТИЧНО: пропускаем OPTIONS запросы (CORS preflight)
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        Method handlerMethod = findHandlerMethod(exchange);

        if (handlerMethod == null) {
            return chain.filter(exchange);
        }

        // Проверяем @PublicEndpoint
        if (handlerMethod.isAnnotationPresent(PublicEndpoint.class)) {
            return chain.filter(exchange);
        }

        // Извлекаем токен
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.validateToken(token);

            String userId = claims.getSubject();
            String email = claims.get("email", String.class);
            List<String> userRoles = claims.get("roles", List.class);

            exchange.getAttributes().put("userId", userId);
            exchange.getAttributes().put("email", email);
            exchange.getAttributes().put("roles", userRoles);

            // Проверяем @RequireRoles
            if (handlerMethod.isAnnotationPresent(RequireRoles.class)) {
                RequireRoles requireRoles = handlerMethod.getAnnotation(RequireRoles.class);
                String[] requiredRoles = requireRoles.value();

                boolean hasRequiredRole = false;
                for (String requiredRole : requiredRoles) {
                    if (userRoles.contains(requiredRole)) {
                        hasRequiredRole = true;
                        break;
                    }
                }

                if (!hasRequiredRole) {
                    return forbidden(exchange.getResponse(),
                            "Insufficient permissions. Required roles: " + String.join(", ", requiredRoles));
                }
            }

            return chain.filter(exchange);

        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "Invalid JWT token: " + e.getMessage());
        }
    }

    private Method findHandlerMethod(ServerWebExchange exchange) {
        try {
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo info = entry.getKey();
                if (info.getMatchingCondition(exchange) != null) {
                    return entry.getValue().getMethod();
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"code\":\"401\",\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private Mono<Void> forbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"code\":\"403\",\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
