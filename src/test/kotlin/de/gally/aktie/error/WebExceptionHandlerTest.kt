package de.gally.aktie.error

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WebExceptionHandlerTest {

    private val cut = WebExceptionHandler()
    private val message = "I know it should not be possible but my application threw an unhandled exception"

    @Test
    fun `test if a throwable is mapped to a responseEntity`() {
        assertStatusAndErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, message) {
            cut.handleExceptions(Throwable(message))
        }
    }

    @Test
    fun `test if an exception is mapped to a responseEntity`() {
        assertStatusAndErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, message) {
            cut.handleExceptions(Exception(message))
        }
    }

    @Test
    fun `test if ServiceIsUnavailableException is correctly mapped`() {
        val cause = "Service is currently not available"
        assertStatusAndErrorMessage(HttpStatus.BAD_REQUEST, cause) {
            cut.handleExceptions(ServiceIsUnavailableException(cause))
        }
    }

    private fun assertStatusAndErrorMessage(
        status: HttpStatus,
        message: String,
        call: () -> Mono<ResponseEntity<Message>>
    ) {
        val responseEntity = ResponseEntity
            .status(status)
            .body(Message(message))

        StepVerifier.create(call())
            .expectNext(responseEntity)
            .verifyComplete()
    }
}
