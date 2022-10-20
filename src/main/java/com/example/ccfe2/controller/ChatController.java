package com.example.ccfe2.controller;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.example.ccfe2.model.ChatMessage;
import com.example.ccfe2.pubsub.RedisPublisher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@CrossOrigin
public class ChatController {
    
	private final RedisPublisher redisPublisher;

	@MessageMapping("/chat/message")
	public void chatMessage(ChatMessage chatMessage) throws Exception {

		ChannelTopic channelTopic = new ChannelTopic("chat9901");
		redisPublisher.publish(channelTopic, chatMessage);
	}

	@MessageMapping("/chat/notice")
	public void chatNotice(ChatMessage chatMessage) throws Exception {

		// ChannelTopic channelTopic = new ChannelTopic("chatNotice");
		// redisPublisher.publish(channelTopic, chatMessage);

		ChannelTopic channelTopic1 = new ChannelTopic("chat9901");
		redisPublisher.publish(channelTopic1, chatMessage);
		ChannelTopic channelTopic2 = new ChannelTopic("chat9902");
		redisPublisher.publish(channelTopic2, chatMessage);
	}

}
