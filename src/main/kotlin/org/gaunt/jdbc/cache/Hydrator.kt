package org.gaunt.jdbc.cache

import org.gaunt.jdbc.redis.client.RedisClient
import org.gaunt.jdbc.redis.model.Message
import org.gaunt.jdbc.redis.subscriber.RedisMessageSubscriber
import org.gaunt.jdbc.user.User
import org.gaunt.jdbc.user.UserDao
import org.springframework.stereotype.Service
import java.lang.Long.parseLong

@Service
class Hydrator(
    private val userDao: UserDao,
    private val redisClient: RedisClient
): RedisMessageSubscriber() {
    private val logger = org.slf4j.LoggerFactory.getLogger(Hydrator::class.java)
    override fun handleMessage(message: Message) {
        logger.info("Received message: $message")
        parseLong(message.payload).let { userId ->
            logger.info("Hydrating user: $userId")
            val user = fetchData(userId)
            user?.let {
                redisClient.set(it.id.toString(), it)
            }
        }
    }

    // Here I'm just fetching the record from the db as an example,
    // but in practice this could be an http call to fetch whatever
    // relations are needed and hydrate them into the cache
    fun fetchData(id: Long): User? {
        return userDao.findById(id)
    }

}