package com.samyookgoo.palgoosam.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.setHostName("localhost");  // 명시적으로 설정
        factory.setPort(6379);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        //call은 직접적인 호출을 의미할 때 주로 사용하며, 간접적인 호출을 의미할 때는 invoke를 주로 사용한다
        // 이게 모든 세팅이 완료되었는지 확인하는 코드임.
        // 필수 설정, 기본 Serializer 설정, 내부 객체 초기화, 연결 테스트 등등...
        // 스프링이 관리하는 Bean으로 등록하면 자동 호출되므로 수동 생성할 때처럼 굳이 호출하지 않아도 됨.
        // 그래도 명시적으로 호출해도, 중복 호출 방어 로직이 있어서 괜찮긴 함.
        template.afterPropertiesSet();

        return template;
    }

//    @Bean
//    public ChannelTopic auctionStatusTopic() {
//        return new ChannelTopic("auction:status");
//    }
//
//    @Bean
//    public ChannelTopic lockReleaseTopic() {
//        return new ChannelTopic("lock.release");
//    }
}