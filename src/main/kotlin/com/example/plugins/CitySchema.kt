package com.example.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

@Serializable
data class Book(
    val name: String,
    val yearOfPublication : String,
    val author : String
)

class BookService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CITIES =
            "CREATE TABLE CITIES (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), POPULATION INT);"
        private const val SELECT_ALL_BOOK = "SELECT * FROM library.book"
        private const val INSERT_CITY = "INSERT INTO library.book (name, yearOfPublication, author) VALUES (?, ?, ?, ?)"
        private const val UPDATE_CITY = "UPDATE library.book SET name = ?, yearOfPublication = ?, author = ? WHERE id = ?"
        private const val DELETE_CITY = "DELETE FROM library.book WHERE book.id = ?"

    }

    init {
//        val statement = connection.createStatement()
//        statement.executeUpdate(CREATE_TABLE_CITIES)
    }
    // Create new city
    suspend fun create(book: Book): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_CITY, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, book.name)
        statement.setString(2, book.yearOfPublication)
        statement.setString(3, book.author)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    // Read a city
    suspend fun read(): List<Book> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_BOOK)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val listBook = mutableListOf<Book>()
            while (!resultSet.isClosed){
                val name = resultSet.getString("name")
                val yearOfPublication = resultSet.getString("yearOfPublication")
                val author = resultSet.getString("author")
                listBook.add(
                    Book(
                        name,
                        yearOfPublication,
                        author
                    )
                )
            }

            return@withContext listBook
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a city
    suspend fun update(id: Int, book: Book) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CITY)
        statement.setString(1, book.name)
        statement.setString(2, book.yearOfPublication)
        statement.setString(3, book.author)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CITY)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}
