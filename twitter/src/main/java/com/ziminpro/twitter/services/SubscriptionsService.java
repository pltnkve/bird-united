package com.ziminpro.twitter.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dao.SubscriptionRepository;
import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class SubscriptionsService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    public Mono<ResponseEntity<Map<String, Object>>> getSubscriptionsForSubscriberById(UUID subscriberId) {
        Map<String, Object> response = new HashMap<>();
        Subscription subscriptions = subscriptionRepository.getSubscription(subscriberId);

        if (subscriptions.getSubscriber() == null) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "Subscriptions for user with ID " + subscriberId + " is not found");
            response.put(Constants.DATA, subscriptions);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Subscriptions have been retrieved");
            response.put(Constants.DATA, subscriptions);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> createSubscription(Subscription subscription) {
        Map<String, Object> response = new HashMap<>();
        boolean success = subscriptionRepository.createSubscription(subscription);

        if (!success) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Subscription has not been created");
            response.put(Constants.DATA, false);
        } else {
            response.put(Constants.CODE, "201");
            response.put(Constants.MESSAGE, "Subscription has been created");
            response.put(Constants.DATA, true);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> updateSubscriptionForSubscriberById(Subscription subscription) {
        Map<String, Object> response = new HashMap<>();
        boolean success = subscriptionRepository.updateSubscription(subscription);

        if (!success) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Subscription has not been updated");
            response.put(Constants.DATA, false);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Subscription has been updated");
            response.put(Constants.DATA, true);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> deleteSubscriptionForSubscriberById(UUID subscriberId) {
        Map<String, Object> response = new HashMap<>();
        boolean success = subscriptionRepository.deleteSubscription(subscriberId);

        if (!success) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Subscription has not been deleted");
            response.put(Constants.DATA, false);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Subscription has been deleted");
            response.put(Constants.DATA, true);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }
}
