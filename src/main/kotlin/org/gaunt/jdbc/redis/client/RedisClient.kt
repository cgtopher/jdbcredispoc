package org.gaunt.jdbc.redis.client

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisClient(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun <T> set(key: String, value: T, ttl: Long? = null) {
        redisTemplate.opsForValue().set(key, value as Any)
        ttl?.let {
            redisTemplate.expire(key, it, TimeUnit.SECONDS)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return redisTemplate.opsForValue().get(key) as T?
    }

    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key) ?: false
    }

    fun setExpire(key: String, ttl: Long) {
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS)
    }
}