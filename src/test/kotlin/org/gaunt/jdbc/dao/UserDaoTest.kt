package org.gaunt.jdbc.dao

import org.gaunt.jdbc.config.DaoConfig
import org.gaunt.jdbc.user.User
import org.gaunt.jdbc.user.UserDao
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import javax.sql.DataSource

@SpringBootTest
@Import(DaoConfig::class)
class UserDaoTest {

    @Autowired
    private lateinit var dataSource: DataSource
    
    private lateinit var userDao: UserDao
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    @BeforeEach
    fun init() {
        // Manually create UserDao to ensure it's properly initialized
        userDao = UserDao(dataSource)
        println("[DEBUG_LOG] UserDao initialized with dataSource: $dataSource")
        println("[DEBUG_LOG] JdbcTemplate in test: $jdbcTemplate")
    }

    @BeforeEach
    fun setup() {
        // Clean up the users table before each test
        jdbcTemplate.execute("DELETE FROM users")
    }

    @Test
    fun testInsertAndFindById() {
        // Create a new user
        val user = User(
            username = "johndoe",
            email = "john.doe@example.com",
            firstName = "John",
            lastName = "Doe"
        )

        // Insert the user and get the generated ID
        val userId = userDao.insert(user)
        
        // Verify the ID is not null
        assertNotNull(userId)
        
        // Find the user by ID
        val foundUser = userDao.findById(userId)
        
        // Verify the user was found and has the correct properties
        assertNotNull(foundUser)
        assertEquals("johndoe", foundUser?.username)
        assertEquals("john.doe@example.com", foundUser?.email)
        assertEquals("John", foundUser?.firstName)
        assertEquals("Doe", foundUser?.lastName)
    }

    @Test
    fun testUpdate() {
        // Create and insert a user
        val user = User(
            username = "janedoe",
            email = "jane.doe@example.com",
            firstName = "Jane",
            lastName = "Doe"
        )
        val userId = userDao.insert(user)
        
        // Create an updated user with the same ID
        val updatedUser = User(
            id = userId,
            username = "janedoe",
            email = "jane.doe@example.com",
            firstName = "Jane Updated",
            lastName = "Doe Updated"
        )
        
        // Update the user
        val rowsAffected = userDao.update(updatedUser)
        
        // Verify one row was affected
        assertEquals(1, rowsAffected)
        
        // Find the user by ID
        val foundUser = userDao.findById(userId)
        
        // Verify the user was updated
        assertNotNull(foundUser)
        assertEquals("Jane Updated", foundUser?.firstName)
        assertEquals("Doe Updated", foundUser?.lastName)
    }

    @Test
    fun testDeleteById() {
        // Create and insert a user
        val user = User(
            username = "userToDelete",
            email = "delete.me@example.com",
            firstName = "Delete",
            lastName = "Me"
        )
        val userId = userDao.insert(user)
        
        // Verify the user exists
        assertNotNull(userDao.findById(userId))
        
        // Delete the user
        val rowsAffected = userDao.deleteById(userId)
        
        // Verify one row was affected
        assertEquals(1, rowsAffected)
        
        // Verify the user no longer exists
        assertNull(userDao.findById(userId))
    }

    @Test
    fun testFindAll() {
        // Create and insert multiple users
        val user1 = User(
            username = "user1",
            email = "user1@example.com",
            firstName = "User",
            lastName = "One"
        )
        val user2 = User(
            username = "user2",
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two"
        )
        userDao.insert(user1)
        userDao.insert(user2)
        
        // Find all users
        val users = userDao.findAll()
        
        // Verify the correct number of users was found
        assertEquals(2, users.size)
    }

    @Test
    fun testFindByUsername() {
        // Create and insert a user
        val user = User(
            username = "findme",
            email = "find.me@example.com",
            firstName = "Find",
            lastName = "Me"
        )
        userDao.insert(user)
        
        // Find the user by username
        val users = userDao.findByUsername("findme")
        
        // Verify the user was found
        assertEquals(1, users.size)
        assertEquals("findme", users[0].username)
    }

    @Test
    fun testFindByEmail() {
        // Create and insert a user
        val user = User(
            username = "emailtest",
            email = "email.test@example.com",
            firstName = "Email",
            lastName = "Test"
        )
        userDao.insert(user)
        
        // Find the user by email
        val foundUser = userDao.findByEmail("email.test@example.com")
        
        // Verify the user was found
        assertNotNull(foundUser)
        assertEquals("emailtest", foundUser?.username)
    }

    @Test
    fun testUniqueConstraints() {
        // Create and insert a user
        val user1 = User(
            username = "unique",
            email = "unique@example.com",
            firstName = "Unique",
            lastName = "User"
        )
        userDao.insert(user1)
        
        // Create another user with the same username
        val user2 = User(
            username = "unique",
            email = "different@example.com",
            firstName = "Different",
            lastName = "Email"
        )
        
        // Verify that inserting a user with a duplicate username throws an exception
        assertThrows(DataIntegrityViolationException::class.java) {
            userDao.insert(user2)
        }
        
        // Create another user with the same email
        val user3 = User(
            username = "different",
            email = "unique@example.com",
            firstName = "Different",
            lastName = "Username"
        )
        
        // Verify that inserting a user with a duplicate email throws an exception
        assertThrows(DataIntegrityViolationException::class.java) {
            userDao.insert(user3)
        }
    }
}