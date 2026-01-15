package com.ziminpro.twitter.controllers;

import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Subscription;
import com.ziminpro.twitter.security.RequireRoles;
import com.ziminpro.twitter.security.SecurityUtils;
import com.ziminpro.twitter.services.SubscriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class SubscriptionController {

    @Autowired
    private SubscriptionsService subscriptionsService;

    // Подписчик может читать только СВОИ подписки
    @RequireRoles({"SUBSCRIBER", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_SUBSCRIPTION + "/{subscriber-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getSubscriptionBySubscriberId(
            @PathVariable(value = "subscriber-id", required = true) UUID subscriberId,
            ServerWebExchange exchange) {

        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.equals(subscriberId)) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only view your own subscriptions", "data", null)
            ));
        }

        return subscriptionsService.getSubscriptionsForSubscriberById(subscriberId);
    }

    // Подписчик может изменить только СВОИ подписки
    @RequireRoles({"SUBSCRIBER", "ADMIN"})
    @RequestMapping(method = RequestMethod.PUT, path = Constants.URI_SUBSCRIPTIONS, consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> updateSubscription(
            @RequestBody Subscription subscription,
            ServerWebExchange exchange) {

        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.equals(subscription.getSubscriber())) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only update your own subscriptions", "data", null)
            ));
        }

        return subscriptionsService.updateSubscriptionForSubscriberById(subscription);
    }

    // Подписчик может создать только СВОИ подписки
    @RequireRoles({"SUBSCRIBER", "ADMIN"})
    @RequestMapping(method = RequestMethod.POST, path = Constants.URI_SUBSCRIPTIONS, consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> createSubscription(
            @RequestBody Subscription subscription,
            ServerWebExchange exchange) {

        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.equals(subscription.getSubscriber())) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only create subscriptions for yourself", "data", null)
            ));
        }

        return subscriptionsService.createSubscription(subscription);
    }

    // Подписчик может удалить только СВОИ подписки
    @RequireRoles({"SUBSCRIBER", "ADMIN"})
    @RequestMapping(method = RequestMethod.DELETE, path = Constants.URI_SUBSCRIPTION + "/{subscriber-id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteSubscription(
            @PathVariable(value = "subscriber-id", required = true) UUID subscriberId,
            ServerWebExchange exchange) {

        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.equals(subscriberId)) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only delete your own subscriptions", "data", null)
            ));
        }

        return subscriptionsService.deleteSubscriptionForSubscriberById(subscriberId);
    }
}
