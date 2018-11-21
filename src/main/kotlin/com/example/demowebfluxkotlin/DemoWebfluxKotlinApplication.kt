package com.example.demowebfluxkotlin

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import java.time.Duration
import java.time.LocalDate

@SpringBootApplication
class DemoWebfluxKotlinApplication

fun main(args: Array<String>) {
    runApplication<DemoWebfluxKotlinApplication>(*args) {
        addInitializers(beans())
    }
}

fun beans() = beans {
    bean {
        CommandLineRunner {
            ref<PersonRepository>().insert(
                arrayListOf(Person(1, "John", "Doe", LocalDate.of(1970, 1, 1)),
                    Person(2, "Jane", "Doe", LocalDate.of(1970, 1, 1)),
                    Person(3, "Brian", "Goetz"))
            ).blockLast(Duration.ofSeconds(2))
        }
    }
    bean {
        PersonRoutes(PersonHandler(ref())).routes()
    }
}

class PersonRoutes(private val handler: PersonHandler) {
    fun routes() = router {
        "/person".nest {
            GET("/{id}", handler::readOne)
            GET("/", handler::readAll)
        }
    }
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll(request: ServerRequest) = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
}

@Document
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : ReactiveMongoRepository<Person, Long>
