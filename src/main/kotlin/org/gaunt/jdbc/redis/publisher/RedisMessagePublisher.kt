package org.gaunt.jdbc.redis.publisher

import org.gaunt.jdbc.redis.model.Message
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import java.util.UUID

@Service
abstract class RedisMessagePublisher<T>(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val channelTopic: ChannelTopic
) {
    private val logger = LoggerFactory.getLogger(RedisMessagePublisher::class.java)

    abstract fun emitEntityChange(entity: T)


    /**
     * Publishes a message with the given payload to the Redis channel
     * 
     * @param payload The message payload to publish
     * @return The published message
     */
    fun publish(payload: String): Message {
        val message = Message(
            id = UUID.randomUUID().toString(),
            payload = payload
        )

        redisTemplate.convertAndSend(channelTopic.topic, message)

        return message
    }


    /**
     * Publishes a pre-constructed message to the Redis channel
     * 
     * @param message The message to publish
     */
    fun publish(message: Message) {
        logger.info("Publishing message: $message to channel: ${channelTopic.topic}")
        redisTemplate.convertAndSend(channelTopic.topic, message)
    }
}