package org.gaunt.jdbc.dao

import java.sql.ResultSet

/**
 * Interface for mapping rows of a ResultSet to objects.
 * 
 * @param T the type of object that this mapper will create from ResultSet rows
 */
interface RowMapper<T> {
    /**
     * Maps a single row of a ResultSet to an object.
     * 
     * @param rs the ResultSet to map
     * @param rowNum the number of the current row
     * @return the result object for the current row
     * @throws java.sql.SQLException if a SQLException is encountered getting column values
     */
    fun mapRow(rs: ResultSet, rowNum: Int): T
}