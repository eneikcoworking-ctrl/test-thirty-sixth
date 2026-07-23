package com.eneik.generated.websocket;

import com.eneik.generated.dto.LiveChatClientPayload;
import com.eneik.generated.dto.LiveChatServerPayload;
import com.eneik.generated.event.MessageBroadcastEvent;
import com.eneik.generated.service.LiveChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LiveChatWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;
    private final LiveChatService liveChatService;

    public LiveChatWebSocketHandler(ObjectMapper objectMapper, LiveChatService liveChatService) {
        this.objectMapper = objectMapper;
        this.liveChatService = liveChatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("New WebSocket connection established: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payloadString = message.getPayload();
        logger.info("Received WebSocket text message: {}", payloadString);

        try {
            LiveChatClientPayload clientPayload = objectMapper.readValue(payloadString, LiveChatClientPayload.class);
            if ("reply".equalsIgnoreCase(clientPayload.getCommand())) {
                if (clientPayload.getDialogId() == null || clientPayload.getText() == null) {
                    throw new IllegalArgumentException("dialogId and text are required for 'reply' command");
                }
                // Process the manual reply: this immediately blocks AI generation and saves the message
                liveChatService.processManualReply(clientPayload.getDialogId(), clientPayload.getText());
            } else {
                logger.warn("Received unknown WebSocket command: {}", clientPayload.getCommand());
                sendError(session, "Unknown command: " + clientPayload.getCommand());
            }
        } catch (Exception e) {
            logger.error("Error processing WebSocket message: {}", payloadString, e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageBroadcast(MessageBroadcastEvent event) {
        LiveChatServerPayload payload = event.getPayload();
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(jsonPayload);
            logger.info("Broadcasting message to {} sessions", sessions.size());
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        logger.error("Failed to send WebSocket message to session: {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to serialize or broadcast message payload: {}", payload, e);
        }
    }

    private void sendError(WebSocketSession session, String errorMsg) {
        if (session.isOpen()) {
            try {
                String errorPayload = objectMapper.writeValueAsString(new ErrorResponse(errorMsg));
                session.sendMessage(new TextMessage(errorPayload));
            } catch (IOException e) {
                logger.error("Failed to send error message to session: {}", session.getId(), e);
            }
        }
    }

    private static class ErrorResponse {
        private final String event = "error";
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getEvent() {
            return event;
        }

        public String getMessage() {
            return message;
        }
    }
}
