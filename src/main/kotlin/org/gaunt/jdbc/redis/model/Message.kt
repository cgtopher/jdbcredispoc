package org.gaunt.jdbc.redis.model

import java.io.Serializable
import java.time.LocalDateTime

data class Message(
    val id: String,
    val payload: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : Serializable