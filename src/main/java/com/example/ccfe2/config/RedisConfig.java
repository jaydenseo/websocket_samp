package com.example.ccfe2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.ccfe2.pubsub.RedisSubscriber;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

	@Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

	/**
	 * 어플리케이션에서 사용할 redisTemplate 설정
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		return redisTemplate;
	}

	/**
	 * 리스너어댑터 설정
	 */
    @Bean
    MessageListenerAdapter messageListenerAdapter(RedisSubscriber redisSubService) {
        return new MessageListenerAdapter(redisSubService);
    }

    /**
	 * redis pub/sub 메시지를 처리하는 listener 설정
	 */
	@Bean
	public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory, MessageListenerAdapter messageListenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(messageListenerAdapter, new ChannelTopic("chat9901"));
		container.addMessageListener(messageListenerAdapter, new ChannelTopic("chatNotice"));
		return container;
	}

	//pub/sub 토픽 설정
    // @Bean
    // ChannelTopic topic() {
    //     return new ChannelTopic("chat9901");
    // }
}
