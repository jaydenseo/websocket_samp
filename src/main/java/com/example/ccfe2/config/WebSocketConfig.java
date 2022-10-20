package com.example.ccfe2.config;

import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");        // 클라이언트로 메세지를 응답해줄 때 prefix를 정의
        config.setApplicationDestinationPrefixes("/app");       // 클라이언트에서 메세지 송신 시 붙여줄 prefix를 정의
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                // .setAllowedOrigins("*")
                // .setAllowedOriginsPatterns("*")
                .withSockJS();            // 최초 소켓 연결을 하는 경우, endpoint가 되는 url
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor(){
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                String sessionId = accessor.getSessionId();

                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    String userId = Objects.toString(accessor.getFirstNativeHeader("userId"), "");
                    String hostIp = Objects.toString(accessor.getFirstNativeHeader("hostIp"), "");
                    String connectTime = Objects.toString(accessor.getFirstNativeHeader("connectTime"), "");
                    String requestUrl = Objects.toString(accessor.getFirstNativeHeader("requestUrl"), "");
                    
                    Map<String, Object> map = accessor.getSessionAttributes();
                    map.put("userId", userId);
                    map.put("hostIp", hostIp);
                    map.put("connectTime", connectTime);
                    map.put("requestUrl", requestUrl);
                    accessor.setSessionAttributes(map);

                    log.info("[ClientInboundChannel] [CONNECT] [{} | {} | {}]" + " >>> " + accessor.getSessionAttributes().toString(), sessionId, userId, hostIp);
                }
                else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();

                    log.info("[ClientInboundChannel] [SUBSCRIBE] [{} | {} | {}]" + " >>> " + accessor.getDestination(), sessionId, userId, hostIp);
                }
                else if(StompCommand.SEND.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();
                    String messagePayload = new String((byte[])message.getPayload());
                    log.info("[ClientInboundChannel] [SEND] [{} | {} | {}]" + " >>> " + messagePayload, sessionId, userId, hostIp);
                }
                else if(StompCommand.DISCONNECT.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();
                    String messagePayload = new String((byte[])message.getPayload());
                    log.info("[ClientInboundChannel] [DISCONNECT] [{} | {} | {}]" + " >>> " + accessor.getSessionAttributes().toString(), sessionId, userId, hostIp);
                }
                return message;                
            }
        });
    }
}
