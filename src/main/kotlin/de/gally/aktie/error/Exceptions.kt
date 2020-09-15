package de.gally.aktie.error

import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDateTime

sealed class ShareApplication(cause: String) : Throwable(cause)

open class ServiceIsUnavailableException(cause: String) : ShareApplication(cause)
open class InternalErrorException(cause: String) : ShareApplication(cause)
open class ClientUnknownHostException(cause: String) : ShareApplication(cause)

/** If an error is thrown this will parse the error to a [ResponseEntity] */
fun Throwable.toErrorResponse(): Mono<ResponseEntity<Message>> {
    fun respondWith(httpStatus: HttpStatus, message: String) = ResponseEntity
        .status(httpStatus)
        .body(Message(message))
        .toMono()

    return when (this) {
        is ShareApplication -> return when (this) {
            is ServiceIsUnavailableException -> respondWith(HttpStatus.BAD_REQUEST, this.localizedMessage)
            is InternalErrorException -> respondWith(HttpStatus.I_AM_A_TEAPOT, this.localizedMessage)
            is ClientUnknownHostException -> respondWith(HttpStatus.BAD_GATEWAY, this.localizedMessage)
        }
        else -> respondWith(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "I know it should not be possible but my application threw an unhandled exception"
        )
    }
}

/** Creates a JSON exception message info box */
data class Message(val message: String) {
    override fun toString() = JSONObject()
        .put("message", message)
        .put("timestamp", LocalDateTime.now())
        .toString()
}
