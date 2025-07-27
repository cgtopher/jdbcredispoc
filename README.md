# JDBC Redis POC

This project demonstrates the use of JDBC with Redis for caching and messaging.

## Redis PubSub Implementation

This project includes a Redis PubSub implementation that allows for real-time messaging between components.

### Components

- **Message Model**: A simple data class representing messages exchanged through Redis PubSub.
- **RedisMessagePublisher**: A service for publishing messages to Redis channels.
- **RedisMessageSubscriber**: A service for receiving messages from Redis channels.
- **RedisConfig**: Configuration class that sets up the necessary beans for Redis PubSub.
- **RedisPubSubDemo**: A command-line runner that demonstrates the PubSub functionality when the application starts.

### Running the Application

1. Start Redis using Docker Compose:
   ```
   docker-compose up -d
   ```

2. Run the application:
   ```
   ./gradlew bootRun
   ```

   The RedisPubSubDemo will automatically run and demonstrate the PubSub functionality.

### Testing

The project includes a test class (`RedisPubSubTest`) that verifies the PubSub functionality.

To run the tests:

1. Make sure Redis is running:
   ```
   docker-compose up -d
   ```

2. Run the tests:
   ```
   ./gradlew test --tests "org.gaunt.jdbc.redis.RedisPubSubTest"
   ```

### Using the PubSub in Your Code

To publish messages:

```kotlin
@Autowired
private lateinit var redisMessagePublisher: RedisMessagePublisher

// Publish a simple message
val message = redisMessagePublisher.publish("Hello, Redis!")

// Or create and publish a custom message
val customMessage = Message(
    id = UUID.randomUUID().toString(),
    payload = "Custom message"
)
redisMessagePublisher.publish(customMessage)
```

To receive messages, inject the RedisMessageSubscriber into your component:

```kotlin
@Autowired
private lateinit var redisMessageSubscriber: RedisMessageSubscriber

// Access received messages
val messages = redisMessageSubscriber.receivedMessages
```

For more complex scenarios, you can implement your own subscriber by creating a class that implements the MessageListener interface or by using the MessageListenerAdapter.