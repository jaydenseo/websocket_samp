package com.example.ccfe2.config;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

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

        // 웹소켓 연결을 하는 경우, endpoint가 되는 url (ws://localhost:9091/websocket)
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(null)
                .addInterceptors(handshakeInterceptor());

        // SockJs로 웹소켓 연결을 하는 경우, endpoint가 되는 url (http://localhost:9091/websocket)
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                // .addInterceptors(handshakeInterceptor())
                .withSockJS()
                .setInterceptors(handshakeInterceptor()); // SockJS의 경우, .addInterceptors 대신 .setInterceptors를 사용할 수도 있다.  
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletServerRequest = (ServletServerHttpRequest) request;
                    HttpServletRequest servletRequest = servletServerRequest.getServletRequest();

                    attributes.put("remoteAddr", servletRequest.getRemoteAddr());
                }
                return true;
            }
            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            }
        };
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor(){
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                String sessionId = accessor.getSessionId();
                String remoteAddr = Objects.toString(accessor.getSessionAttributes().get("remoteAddr"), "");

                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    // StompHeaders headers = StompHeaders.readOnlyStompHeaders(accessor.toNativeHeaderMap());
                    String userId = Objects.toString(accessor.getFirstNativeHeader("userId"), "");
                    String hostIp = Objects.toString(accessor.getFirstNativeHeader("hostIp"), "");
                    
                    Map<String, Object> map = accessor.getSessionAttributes();
                    map.put("userId", userId);
                    map.put("hostIp", hostIp);
                    accessor.setSessionAttributes(map);

                    log.info("[ClientInboundChannel] [CONNECT] [{} | {} | {} | {}]" + " >>> " + accessor.getSessionAttributes().toString(), sessionId, userId, hostIp, remoteAddr);
                }
                else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();

                    log.info("[ClientInboundChannel] [SUBSCRIBE] [{} | {} | {} | {}]" + " >>> " + accessor.getDestination(), sessionId, userId, hostIp, remoteAddr);
                }
                else if(StompCommand.SEND.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();
                    String messagePayload = new String((byte[])message.getPayload());
                    log.info("[ClientInboundChannel] [SEND] [{} | {} | {} | {}]" + " >>> " + messagePayload, sessionId, userId, hostIp, remoteAddr);
                }
                else if(StompCommand.DISCONNECT.equals(accessor.getCommand())){

                    String userId = accessor.getSessionAttributes().get("userId").toString();
                    String hostIp = accessor.getSessionAttributes().get("hostIp").toString();
                    String messagePayload = new String((byte[])message.getPayload());
                    log.info("[ClientInboundChannel] [DISCONNECT] [{} | {} | {} | {}]" + " >>> " + accessor.getSessionAttributes().toString(), sessionId, userId, hostIp, remoteAddr);
                }
                return message;                
            }
        });
    }


}
