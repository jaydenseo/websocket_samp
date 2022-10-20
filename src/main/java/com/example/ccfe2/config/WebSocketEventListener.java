package com.example.ccfe2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.ccfe2.model.ChatMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Received a new web socket connection. Session ID : [{}]", headerAccessor.getSessionId());

        StompHeaderAccessor headerAccessor2 = StompHeaderAccessor.wrap((Message<byte[]>)event.getMessage().getHeaders().get("simpConnectMessage"));
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.ENTER);
        chatMessage.setMessage("["+headerAccessor2.getSessionAttributes().get("userId").toString()+"] 님이 입장하셨습니다.");
        messagingTemplate.convertAndSend("/topic/system", chatMessage);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Web socket session closed. Session ID : [{}]", headerAccessor.getSessionId());
        
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(ChatMessage.MessageType.QUIT);
        chatMessage.setMessage("["+headerAccessor.getSessionAttributes().get("userId").toString()+"] 님이 퇴장하셨습니다.");
        messagingTemplate.convertAndSend("/topic/system", chatMessage);
    }
}
