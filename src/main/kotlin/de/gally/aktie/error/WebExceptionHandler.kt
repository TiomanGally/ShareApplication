package de.gally.aktie.error

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

@Primary
@ControllerAdvice
class WebExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Throwable::class)
    @RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleExceptions(exception: Throwable): Mono<ResponseEntity<Message>> {
        logger.error("Message of Exception: [${exception.message}]")
        return exception.toErrorResponse()
    }
}
