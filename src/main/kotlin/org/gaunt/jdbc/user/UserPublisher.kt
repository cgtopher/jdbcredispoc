package org.gaunt.jdbc.user

import org.gaunt.jdbc.redis.client.RedisClient
import org.gaunt.jdbc.redis.publisher.RedisMessagePublisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class UserPublisher(
    private val redisClient: RedisClient,
    redisTemplate: RedisTemplate<String, Any>,
    channelTopic: ChannelTopic
) : RedisMessagePublisher<User>(redisTemplate, channelTopic) {
    override fun emitEntityChange(entity: User) {
        // Expire the record so current operations can abort if necessary
        redisClient.setExpire(entity.toString(), 0)
        // Publish message to trigger a fresh pull into the cache
        this.publish(entity.id.toString())
    }
}