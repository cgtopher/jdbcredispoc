package org.gaunt.jdbc.user

import org.gaunt.jdbc.dao.AbstractJdbcDao
import org.gaunt.jdbc.dao.RowMapper
import org.gaunt.jdbc.user.User
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * DAO implementation for User entities.
 * Extends AbstractJdbcDao to inherit common JDBC operations.
 */
@Repository
class UserDao(dataSource: DataSource, private val userPublisher: UserPublisher) : AbstractJdbcDao<User, Long>(dataSource) {

    companion object {
        private const val TABLE_NAME = "users"
        private const val INSERT_SQL = "INSERT INTO $TABLE_NAME (username, email, first_name, last_name) VALUES (?, ?, ?, ?)"
        private const val UPDATE_SQL = "UPDATE $TABLE_NAME SET username = ?, email = ?, first_name = ?, last_name = ? WHERE id = ?"
        private const val DELETE_BY_ID_SQL = "DELETE FROM $TABLE_NAME WHERE id = ?"
        private const val FIND_BY_ID_SQL = "SELECT id, username, email, first_name, last_name FROM $TABLE_NAME WHERE id = ?"
        private const val FIND_ALL_SQL = "SELECT id, username, email, first_name, last_name FROM $TABLE_NAME"
    }

    override fun getInsertSql(): String = INSERT_SQL

    override fun getUpdateSql(): String = UPDATE_SQL

    override fun getDeleteByIdSql(): String = DELETE_BY_ID_SQL

    override fun getFindByIdSql(): String = FIND_BY_ID_SQL

    override fun getFindAllSql(): String = FIND_ALL_SQL

    override fun getRowMapper(): RowMapper<User> = UserRowMapper()

    override fun setInsertParameters(ps: PreparedStatement, entity: User) {
        ps.setString(1, entity.username)
        ps.setString(2, entity.email)
        ps.setString(3, entity.firstName)
        ps.setString(4, entity.lastName)
    }

    override fun setUpdateParameters(ps: PreparedStatement, entity: User) {
        ps.setString(1, entity.username)
        ps.setString(2, entity.email)
        ps.setString(3, entity.firstName)
        ps.setString(4, entity.lastName)
        ps.setLong(5, entity.id!!) // Safe call with !! as ID should not be null for updates
    }

    override fun extractId(entity: User): Long {
        return entity.id ?: throw IllegalArgumentException("User ID cannot be null")
    }

    /**
     * Find users by username.
     *
     * @param username the username to search for
     * @return a list of users with matching username
     */
    fun findByUsername(username: String): List<User> {
        val sql = "SELECT id, username, email, first_name, last_name FROM $TABLE_NAME WHERE username = ?"
        return query(sql, username)
    }

    /**
     * Find users by email.
     *
     * @param email the email to search for
     * @return a list of users with matching email
     */
    fun findByEmail(email: String): User? {
        val sql = "SELECT id, username, email, first_name, last_name FROM $TABLE_NAME WHERE email = ?"
        val users = query(sql, email)
        return users.firstOrNull()
    }

    /**
     * Inner class that implements RowMapper for User entities.
     */
    private class UserRowMapper : RowMapper<User> {
        override fun mapRow(rs: ResultSet, rowNum: Int): User {
            return User(
                id = rs.getLong("id"),
                username = rs.getString("username"),
                email = rs.getString("email"),
                firstName = rs.getString("first_name"),
                lastName = rs.getString("last_name")
            )
        }
    }
}