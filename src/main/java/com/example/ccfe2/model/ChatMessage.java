package com.example.ccfe2.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ChatMessage {
	private MessageType type;
    private String name;
	private String message;
	private String date;
	
	public ChatMessage() {
		
	}
	
	public ChatMessage(MessageType type, String name, String message, String date) {
		this.type = type;
		this.name = name;
		this.message = message;
		this.date = date;
	}

	public static ChatMessage createChatMessage(MessageType type, String name, String message, String date) {
		ChatMessage chatMessage = ChatMessage.builder()
									.type(type)
									.name(name)
									.message(message)
									.date(date)
									.build();
		return chatMessage;
	}

	public enum MessageType {
		ENTER, 
		QUIT,
		CHAT,
		NOTICE
	}
}
