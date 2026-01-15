package com.ziminpro.twitter.controllers;

import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.security.RequireRoles;
import com.ziminpro.twitter.security.SecurityUtils;
import com.ziminpro.twitter.services.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class MessageController {

    @Autowired
    private MessagesService messages;

    // Все авторизованные могут читать сообщение
    @RequireRoles({"ADMIN", "PRODUCER", "SUBSCRIBER"})
    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagebyId(
            @PathVariable(value = "message-id", required = true) String messageId) {
        return messages.getMessagebyId(UUID.fromString(messageId));
    }

    // Все авторизованные могут читать сообщения продюсера
    @RequireRoles({"ADMIN", "PRODUCER", "SUBSCRIBER"})
    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_PRODUCER + "/{producer-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForProducerById(
            @PathVariable(value = "producer-id", required = true) String producerId) {
        return messages.getMessagesForProducerById(UUID.fromString(producerId));
    }

    // Подписчики читают свою ленту
    @RequireRoles({"SUBSCRIBER", "ADMIN"})
    @RequestMapping(method = RequestMethod.GET, path = Constants.URI_SUBSCRIBER + "/{subscriber-id}")
    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForSubscriberById(
            @PathVariable(value = "subscriber-id", required = true) String subscriberId,
            ServerWebExchange exchange) {

        // Юзер может читать только СВОЮ ленту (кроме ADMIN)
        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.toString().equals(subscriberId)) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only view your own feed", "data", null)
            ));
        }

        return messages.getMessagesForSubscriberById(UUID.fromString(subscriberId));
    }

    // Только PRODUCER может создавать сообщения
    @RequireRoles({"PRODUCER", "ADMIN"})
    @RequestMapping(method = RequestMethod.POST, path = Constants.URI_MESSAGE, consumes = Constants.APPLICATION_JSON)
    public Mono<ResponseEntity<Map<String, Object>>> createMessage(
            @RequestBody Message message,
            ServerWebExchange exchange) {

        // Продюсер может создавать сообщения только от своего имени (кроме ADMIN)
        UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);
        if (!SecurityUtils.hasRole(exchange, "ADMIN") && !currentUserId.equals(message.getAuthor())) {
            return Mono.just(ResponseEntity.status(403).body(
                    Map.of("code", "403", "message", "You can only post messages as yourself", "data", null)
            ));
        }

        return messages.createMessage(message);
    }

    // Только автор или ADMIN может удалить сообщение
    @RequireRoles({"PRODUCER", "ADMIN"})
    @RequestMapping(method = RequestMethod.DELETE, path = Constants.URI_MESSAGE + "/{message-id}")
    public Mono<ResponseEntity<Map<String, Object>>> deleteMessageById(
            @PathVariable(value = "message-id", required = true) String messageId,
            ServerWebExchange exchange) {

        return messages.deleteMessageById(UUID.fromString(messageId), exchange);
    }
}
