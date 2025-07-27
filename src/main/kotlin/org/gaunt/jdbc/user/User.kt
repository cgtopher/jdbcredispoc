package org.gaunt.jdbc.user

/**
 * Represents a user in the system.
 *
 * @property id The unique identifier of the user
 * @property username The username of the user
 * @property email The email address of the user
 * @property firstName The first name of the user
 * @property lastName The last name of the user
 */
data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)