package org.gaunt.jdbc.redis.subscriber

import org.gaunt.jdbc.redis.model.Message
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import java.lang.Thread.sleep

abstract class RedisMessageSubscriber : MessageListener {
    private val logger = LoggerFactory.getLogger(RedisMessageSubscriber::class.java)
    private val serializer = GenericJackson2JsonRedisSerializer()

    abstract fun handleMessage(message: Message)

    override fun onMessage(message: org.springframework.data.redis.connection.Message, pattern: ByteArray?) {
        // This is important if we decide to run off of a read replica. Ensure that the copy happened
        sleep(300)
        try {
            val messageBody = message.body
            val deserializedMessage = serializer.deserialize(messageBody, Message::class.java)
            
            logger.info("Received message: $deserializedMessage")

            deserializedMessage?.let { handleMessage(it) } ?: logger.error("Message is null")
        } catch (e: Exception) {
            logger.error("Error processing message: ${e.message}", e)
        }
    }
}