package com.ziminpro.twitter.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ziminpro.twitter.dao.MessageRepository;
import com.ziminpro.twitter.dtos.Constants;
import com.ziminpro.twitter.dtos.Message;
import com.ziminpro.twitter.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Service
public class MessagesService {

    @Autowired
    private MessageRepository messageRepository;

    public Mono<ResponseEntity<Map<String, Object>>> createMessage(Message message) {
        Map<String, Object> response = new HashMap<>();
        UUID messageId = messageRepository.createMessage(message);

        if (messageId == null) {
            response.put(Constants.CODE, "400");
            response.put(Constants.MESSAGE, "Message has not been created");
            response.put(Constants.DATA, "Something went wrong");
        } else {
            response.put(Constants.CODE, "201");
            response.put(Constants.MESSAGE, "Message has been created");
            response.put(Constants.DATA, messageId.toString());
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> getMessagebyId(UUID messageId) {
        Map<String, Object> response = new HashMap<>();
        Message message = messageRepository.getMessagebyId(messageId);

        if (message.getId() == null) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "Message not found");
            response.put(Constants.DATA, message);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Message has been found");
            response.put(Constants.DATA, message);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForProducerById(UUID producerId) {
        Map<String, Object> response = new HashMap<>();
        List<Message> messages = messageRepository.getMessagesForProducerById(producerId);

        if (messages.isEmpty()) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "Either producer didn't produce any messages or producer not found");
            response.put(Constants.DATA, new ArrayList<>());
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "List of messages has been requested successfully");
            response.put(Constants.DATA, messages);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> getMessagesForSubscriberById(UUID subscriberId) {
        Map<String, Object> response = new HashMap<>();
        List<Message> messages = messageRepository.getMessagesForSubscriberById(subscriberId);

        if (messages.isEmpty()) {
            response.put(Constants.CODE, "404");
            response.put(Constants.MESSAGE, "Subscription not found or empty");
            response.put(Constants.DATA, new ArrayList<>());
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "List of messages has been requested successfully");
            response.put(Constants.DATA, messages);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }

    public Mono<ResponseEntity<Map<String, Object>>> deleteMessageById(UUID messageId, ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();

        // Проверяем, что юзер - автор сообщения (кроме ADMIN)
        if (!SecurityUtils.hasRole(exchange, "ADMIN")) {
            Message message = messageRepository.getMessagebyId(messageId);
            UUID currentUserId = SecurityUtils.getCurrentUserId(exchange);

            if (message.getId() == null) {
                response.put(Constants.CODE, "404");
                response.put(Constants.MESSAGE, "Message not found");
                response.put(Constants.DATA, false);
                return Mono.just(ResponseEntity.status(404).body(response));
            }

            if (!message.getAuthor().equals(currentUserId)) {
                response.put(Constants.CODE, "403");
                response.put(Constants.MESSAGE, "You can only delete your own messages");
                response.put(Constants.DATA, false);
                return Mono.just(ResponseEntity.status(403).body(response));
            }
        }

        int result = messageRepository.deleteMessageById(messageId);
        if (result != 1) {
            response.put(Constants.CODE, "500");
            response.put(Constants.MESSAGE, "Message " + messageId + " has not been deleted");
            response.put(Constants.DATA, false);
        } else {
            response.put(Constants.CODE, "200");
            response.put(Constants.MESSAGE, "Message " + messageId + " successfully deleted");
            response.put(Constants.DATA, true);
        }
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_JSON)
                .header(Constants.ACCEPT, Constants.APPLICATION_JSON)
                .body(response));
    }
}
