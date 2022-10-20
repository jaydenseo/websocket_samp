package com.example.ccfe2.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.example.ccfe2.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
	private final RedisTemplate redisTemplate;
	private final SimpMessageSendingOperations messagingTemplate;

	/**
	 * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			log.info("[RedisSubscriber] Message= {}", message);

			// redis에서 발행된 데이터를 받아 deserialize
			String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
			// ChatMessage 객채로 맵핑
			ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
			
			if(ChatMessage.MessageType.CHAT.equals(chatMessage.getType())){
				messagingTemplate.convertAndSend("/topic/chat", chatMessage);
			}
			else if(ChatMessage.MessageType.NOTICE.equals(chatMessage.getType())){
				messagingTemplate.convertAndSend("/topic/notice", chatMessage);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
