package org.gaunt.jdbc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JdbcRedisPocApplication

fun main(args: Array<String>) {
    runApplication<JdbcRedisPocApplication>(*args)
}
