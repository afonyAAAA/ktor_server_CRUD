package com.example.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Date
import java.sql.Statement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
data class Book(
    val id: Int = 0,
    val name: String,
    val dateOfPublication: String,
    val author: String
)

fun Book.toBookEntity() : BookEntity{
    val date = this.dateOfPublication
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatDate = LocalDate.parse(date, formatter)
    val dateSQL = Date.valueOf(formatDate)

    return BookEntity(
        id = this.id,
        name = this.name,
        dateOfPublication = dateSQL,
        author = this.author
    )
}

data class BookEntity(
    val id: Int = 0,
    val name: String,
    val dateOfPublication: Date,
    val author: String
)

class BookService(private val connection: Connection) {
    companion object {
        private const val CREATE_SCHEMA_LIBRARY = "CREATE SCHEMA IF NOT EXISTS library"
        private const val CREATE_TABLE_BOOK = "CREATE TABLE IF NOT EXISTS library.Book(" +
                "\"id\" serial primary key, " +
                "\"name\" varchar(100), " +
                "\"dateofpublication\" date," +
                " \"author\" varchar(100)" +
                ")"
        private const val SELECT_ALL_BOOK = "SELECT * FROM library.book ORDER BY id"
        private const val INSERT_BOOK = "INSERT INTO library.book (name, dateofpublication, author) VALUES (?, ?, ?)"
        private const val UPDATE_BOOK = "UPDATE library.book SET name = ?, dateofpublication = ?, author = ? WHERE id = ?"
        private const val DELETE_ALL_BOOKS = "TRUNCATE TABLE library.book RESTART IDENTITY"
    }

    init {
        connection.prepareStatement(CREATE_SCHEMA_LIBRARY).execute()
        connection.prepareStatement(CREATE_TABLE_BOOK).execute()
    }
    suspend fun create(book: Book): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_BOOK, Statement.RETURN_GENERATED_KEYS)
        val bookEntity = book.toBookEntity()
        statement.setString(1, bookEntity.name)
        statement.setDate(2, bookEntity.dateOfPublication)
        statement.setString(3, bookEntity.author)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted book")
        }
    }
    suspend fun read(): List<Book> = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_ALL_BOOK)
        val resultSet = statement.executeQuery()


        val listBook = mutableListOf<Book>()
        while (resultSet.next() && !resultSet.isClosed){
            val id = resultSet.getInt("id")
            val name = resultSet.getString("name")
            val dateOfPublication = resultSet.getString("dateofpublication")
            val author = resultSet.getString("author")
            listBook.add(
                Book(
                    id,
                    name,
                    dateOfPublication,
                    author
                )
            )
        }

        if (listBook.isNotEmpty()) {
            return@withContext listBook
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun update(id: Int, book: Book) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_BOOK)
        val bookEntity = book.toBookEntity()

        statement.setString(1, bookEntity.name)
        statement.setDate(2, bookEntity.dateOfPublication)
        statement.setString(3, bookEntity.author)
        statement.setInt(4, id)
        statement.executeUpdate()
    }

    suspend fun deleteAllBooks() = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_ALL_BOOKS)
        statement.executeUpdate()
    }
}
