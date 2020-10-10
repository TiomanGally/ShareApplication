package de.gally.aktie.error

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.stream.Stream

class ExceptionsKtTest {

    data class StatusWithThrowable(val status: HttpStatus, val throwable: Throwable)

    private class ExceptionsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = Stream.of(
            StatusWithThrowable(
                HttpStatus.BAD_REQUEST,
                ServiceIsUnavailableException("This service is not available")
            ),
            StatusWithThrowable(
                HttpStatus.I_AM_A_TEAPOT,
                InternalErrorException("Something went really wrong :("),
            ),
            StatusWithThrowable(
                HttpStatus.BAD_GATEWAY,
                ClientUnknownHostException("This is an unknown Client"),
            ),
            StatusWithThrowable(
                HttpStatus.INTERNAL_SERVER_ERROR,
                NullPointerException("I know it should not be possible but my application threw an unhandled exception"),
            ),
        ).map { Arguments.of(it) }
    }


    @DisplayName("Test with ")
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(ExceptionsProvider::class)
    fun exceptionHandlerTest(param: StatusWithThrowable) {
        assertStatusAndErrorMessage(param.status, param.throwable.localizedMessage) {
            param.throwable.toErrorResponse()
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
