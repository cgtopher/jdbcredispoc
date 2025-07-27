package org.gaunt.jdbc.dao

import org.gaunt.jdbc.redis.publisher.RedisMessagePublisher
import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import javax.sql.DataSource

/**
 * Abstract base class for JDBC-based Data Access Objects.
 * Provides common database operations like insert, update, delete, and query.
 *
 * @param T the domain type this DAO manages
 * @param ID the type of the ID of the domain object
 */
abstract class AbstractJdbcDao<T, ID>(dataSource: DataSource, private val publisher: RedisMessagePublisher<T>? = null) {

    protected val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    /**
     * Returns the SQL for inserting a new entity.
     * @return the SQL insert statement
     */
    protected abstract fun getInsertSql(): String

    /**
     * Returns the SQL for updating an existing entity.
     * @return the SQL update statement
     */
    protected abstract fun getUpdateSql(): String

    /**
     * Returns the SQL for deleting an entity by ID.
     * @return the SQL delete statement
     */
    protected abstract fun getDeleteByIdSql(): String

    /**
     * Returns the SQL for finding an entity by ID.
     * @return the SQL select statement
     */
    protected abstract fun getFindByIdSql(): String

    /**
     * Returns the SQL for finding all entities.
     * @return the SQL select statement
     */
    protected abstract fun getFindAllSql(): String

    /**
     * Returns the row mapper for converting ResultSet rows to domain objects.
     * @return the row mapper
     */
    protected abstract fun getRowMapper(): RowMapper<T>

    /**
     * Sets parameters for an insert statement.
     * @param ps the PreparedStatement to set parameters on
     * @param entity the entity to insert
     */
    protected abstract fun setInsertParameters(ps: PreparedStatement, entity: T)

    /**
     * Sets parameters for an update statement.
     * @param ps the PreparedStatement to set parameters on
     * @param entity the entity to update
     */
    protected abstract fun setUpdateParameters(ps: PreparedStatement, entity: T)

    /**
     * Extracts the ID from an entity.
     * @param entity the entity
     * @return the ID of the entity
     */
    protected abstract fun extractId(entity: T): ID

    /**
     * Inserts a new entity and returns the generated ID.
     * @param entity the entity to insert
     * @return the generated ID
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun insert(entity: T): ID {
        val keyHolder: KeyHolder = GeneratedKeyHolder()
        
        jdbcTemplate.update(
            PreparedStatementCreator { connection: Connection ->
                val ps = connection.prepareStatement(
                    getInsertSql(),
                    Statement.RETURN_GENERATED_KEYS
                )
                setInsertParameters(ps, entity)
                ps
            },
            keyHolder
        )

        publisher?.emitEntityChange(entity)
        
        // Extract the generated key
        val key = keyHolder.key
        return key as ID
    }

    /**
     * Updates an existing entity. Publishing any changes to cache
     * @param entity the entity to update
     * @return the number of rows affected
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun update(entity: T): Int {
        val result = jdbcTemplate.update(
            PreparedStatementCreator { connection: Connection ->
                val ps = connection.prepareStatement(getUpdateSql())
                setUpdateParameters(ps, entity)
                ps
            }
        )
        val updatedEntity = findById(extractId(entity))
        if (updatedEntity != null) {
            publisher?.emitEntityChange(updatedEntity)
        }
        return result
    }

    /**
     * Deletes an entity by ID.
     * @param id the ID of the entity to delete
     * @return the number of rows affected
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun deleteById(id: ID): Int {
        return jdbcTemplate.update(getDeleteByIdSql(), id)
    }

    /**
     * Finds an entity by ID.
     * @param id the ID of the entity to find
     * @return the entity, or null if not found
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun findById(id: ID): T? {
        try {
            return jdbcTemplate.queryForObject(
                getFindByIdSql(),
                { rs, rowNum -> getRowMapper().mapRow(rs, rowNum) },
                id
            )
        } catch (e: EmptyResultDataAccessException) {
            return null
        }
    }

    /**
     * Finds all entities.
     * @return a list of all entities
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun findAll(): List<T> {
        return jdbcTemplate.query(
            getFindAllSql()
        ) { rs, rowNum -> getRowMapper().mapRow(rs, rowNum) }
    }

    /**
     * Executes a custom query with parameters.
     * @param sql the SQL query to execute
     * @param params the parameters for the query
     * @return a list of entities matching the query
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun query(sql: String, vararg params: Any): List<T> {
        return jdbcTemplate.query(
            sql,
            { rs, rowNum -> getRowMapper().mapRow(rs, rowNum) },
            *params
        )
    }

    /**
     * Executes a custom update statement with parameters.
     * @param sql the SQL update statement to execute
     * @param params the parameters for the update
     * @return the number of rows affected
     * @throws DataAccessException if there's an error during the database operation
     */
    @Throws(DataAccessException::class)
    fun execute(sql: String, vararg params: Any): Int {
        return jdbcTemplate.update(sql, *params)
    }
}