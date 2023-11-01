package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*

fun Application.configureDatabases() {

    val dbConnection: Connection = connectToPostgres(embedded = true)
    val bookService = BookService(dbConnection)

    routing {
        post("/addBook") {
            val book = call.receive<Book>()
            val id = bookService.create(book)
            call.respond(HttpStatusCode.Created, "ID созданной книги: $id")
        }

        get("/books") {
            try {
                val city = bookService.read()
                if(city.isEmpty()){
                    throw Exception()
                }else{
                    call.respond(HttpStatusCode.OK, city)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, "Таблица книг пуста")
            }
        }

        post("/updateBook") {
            val id = call.request.queryParameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<Book>()
            bookService.update(id, user)
            call.respond(HttpStatusCode.OK, "Данные книги обновлены")
        }

        get("/deleteAllBooks") {
            bookService.deleteAllBooks()
            call.respond(HttpStatusCode.OK, "Все книги... УНИЧТОЖЕНЫ!")
        }
    }
}

/**
 * Makes a connection to a Postgres database.
 *
 * In order to connect to your running Postgres process,
 * please specify the following parameters in your configuration file:
 * - postgres.url -- Url of your running database process.
 * - postgres.user -- Username for database connection
 * - postgres.password -- Password for database connection
 *
 * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
 * and install Postgres and follow the instructions [here](https://postgresapp.com/).
 * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
 * user and password values.
 *
 *
 * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
 * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
 *
 * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
 * your application shuts down by calling [Connection.close]
 * */
fun Application.connectToPostgres(embedded: Boolean): Connection {
    Class.forName("org.postgresql.Driver")
    if (embedded) {
        return DriverManager.getConnection("jdbc:postgresql://postgres:5432/postgres", "postgres", "admin123")
    } else {
        val url = environment.config.property("postgres.url").getString()
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return DriverManager.getConnection(url, user, password)
    }
}
