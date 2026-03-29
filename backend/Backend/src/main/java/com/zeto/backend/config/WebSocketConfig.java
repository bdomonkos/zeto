package com.zeto.backend.config;

import com.zeto.backend.websocket.DeviceWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket configuration that registers the {@link DeviceWebSocketHandler}
 * on the {@code /ws} endpoint with all origins allowed.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DeviceWebSocketHandler handler;

    /**
     * @param handler WebSocket handler for device status updates
     */
    public WebSocketConfig(DeviceWebSocketHandler handler) {
        this.handler = handler;
    }

    /**
     * @param registry the handler registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
                .setAllowedOrigins("*");
    }
}
