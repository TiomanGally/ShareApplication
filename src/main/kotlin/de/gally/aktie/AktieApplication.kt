package de.gally.aktie

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

fun main(args: Array<String>) {
    runApplication<AktieApplication>(*args)
}

@SpringBootApplication
class AktieApplication {

    @Bean
    fun route() = router {
        accept(MediaType.APPLICATION_JSON).nest {
            GET("/info") {
                ServerResponse.ok().bodyValue("v1 of ShareApplication")
            }
            GET("/alive") {
                ServerResponse.ok().bodyValue("App is alive")
            }
        }
    }
}
