package com.example.demowebfluxkotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import io.github.cdimascio.swagger.Validate
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
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
            POST("/", handler::create)
        }
    }
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll(request: ServerRequest) =
        ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) =
        ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
    fun create(request: ServerRequest): Mono<ServerResponse> =
        validate.request(request).withBody(Person::class.java) {
            ok().body(personRepository.save(it))
        }
}

@Document
class Person(@Id val id: Long,
             val firstName: String,
             val lastName: String,
//             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
             val birthdate: LocalDate? = null)

interface PersonRepository : ReactiveMongoRepository<Person, Long>

val validate = Validate.configure("static/api.yaml") { status, messages ->
    Error(status.value(), messages)
}

val dotenv = dotenv()
