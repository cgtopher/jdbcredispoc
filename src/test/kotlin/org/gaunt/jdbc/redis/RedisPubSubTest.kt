package org.gaunt.jdbc.redis

import org.gaunt.jdbc.redis.model.Message
import org.gaunt.jdbc.redis.publisher.RedisMessagePublisher
import org.gaunt.jdbc.redis.subscriber.RedisMessageSubscriber
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test for Redis PubSub functionality.
 * 
 * Note: This test requires Redis to be running. 
 * You can start it using docker-compose up -d
 */
@SpringBootTest
class RedisPubSubTest {

    @Autowired
    private lateinit var redisMessagePublisher: RedisMessagePublisher

    @Autowired
    private lateinit var redisMessageSubscriber: RedisMessageSubscriber

    @Test
    fun testPubSub() {
        // Clear any previous messages
        redisMessageSubscriber.receivedMessages.clear()
        
        // Create and publish a test message
        val testPayload = "Test message ${UUID.randomUUID()}"
        val publishedMessage = redisMessagePublisher.publish(testPayload)
        
        println("[DEBUG_LOG] Published message: $publishedMessage")
        
        // Wait a bit for the message to be processed
        TimeUnit.SECONDS.sleep(1)
        
        // Verify the message was received
        println("[DEBUG_LOG] Received messages: ${redisMessageSubscriber.receivedMessages}")
        assertTrue(redisMessageSubscriber.receivedMessages.isNotEmpty(), "No messages received")
        
        // Find our message in the received messages
        val receivedMessage = redisMessageSubscriber.receivedMessages.find { it.id == publishedMessage.id }
        println("[DEBUG_LOG] Found matching message: $receivedMessage")
        
        // Verify the message content
        assertEquals(publishedMessage.id, receivedMessage?.id, "Message ID doesn't match")
        assertEquals(publishedMessage.payload, receivedMessage?.payload, "Message payload doesn't match")
    }
    
    @Test
    fun testPublishMultipleMessages() {
        // Clear any previous messages
        redisMessageSubscriber.receivedMessages.clear()
        
        // Publish multiple messages
        val numMessages = 5
        val publishedMessages = (1..numMessages).map { 
            redisMessagePublisher.publish("Test message $it")
        }
        
        println("[DEBUG_LOG] Published $numMessages messages")
        
        // Wait a bit for the messages to be processed
        TimeUnit.SECONDS.sleep(2)
        
        // Verify all messages were received
        println("[DEBUG_LOG] Received ${redisMessageSubscriber.receivedMessages.size} messages")
        assertTrue(redisMessageSubscriber.receivedMessages.size >= numMessages, 
            "Not all messages were received. Expected at least $numMessages, got ${redisMessageSubscriber.receivedMessages.size}")
        
        // Verify each published message was received
        for (publishedMessage in publishedMessages) {
            val receivedMessage = redisMessageSubscriber.receivedMessages.find { it.id == publishedMessage.id }
            println("[DEBUG_LOG] Checking message ${publishedMessage.id}: ${receivedMessage != null}")
            assertTrue(receivedMessage != null, "Message with ID ${publishedMessage.id} was not received")
            assertEquals(publishedMessage.payload, receivedMessage?.payload, "Message payload doesn't match")
        }
    }
}