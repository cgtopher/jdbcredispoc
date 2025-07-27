package org.gaunt.jdbc.config

import org.gaunt.jdbc.user.UserDao
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Configuration class for DAO beans.
 */
@Configuration
class DaoConfig {

    /**
     * Creates a UserDao bean with the provided DataSource.
     *
     * @param dataSource the DataSource to use for database operations
     * @return a UserDao instance
     */
    @Bean
    fun userDao(dataSource: DataSource): UserDao {
        return UserDao(dataSource)
    }
}