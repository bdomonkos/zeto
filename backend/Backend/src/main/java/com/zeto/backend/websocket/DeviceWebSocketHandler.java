package com.zeto.backend.websocket;

import com.zeto.backend.model.DeviceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler that maintains active client sessions and broadcasts
 * device status updates to all connected clients.
 */
@Slf4j
@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * @param session the newly connected session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    /**
     * @param session closed session to remove
     * @param status  close reason
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    /**
     * Sends the device status as JSON to all open sessions; closed ones are skipped.
     *
     * @param device status to broadcast
     */
    public void broadcast(DeviceStatus device) {
        try {
            String json = mapper.writeValueAsString(device);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast device status", e);
        }
    }
}
