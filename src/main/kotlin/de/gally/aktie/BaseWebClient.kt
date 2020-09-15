package de.gally.aktie

import de.gally.aktie.error.ClientUnknownHostException
import de.gally.aktie.error.InternalErrorException
import de.gally.aktie.error.ServiceIsUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.ConnectException
import java.net.UnknownHostException

abstract class BaseWebClient {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /** Logs the Request for a WebClient */
    fun log(targetSystem: TargetSystem, method: HttpMethod, uri: String) {
        logger.info("Making [$method] request against [$targetSystem] with uri [$uri]")
    }

    /** This functions handles all errors */
    fun <T> Mono<T>.handleClientError(
        targetSystem: TargetSystem,
        specificCalls: WebClientResponseException.() -> Throwable = { this }
    ): Mono<T> {
        return this.onErrorMap {
            when (it) {
                is WebClientResponseException -> {
                    when (it.statusCode) {
                        HttpStatus.SERVICE_UNAVAILABLE -> ServiceIsUnavailableException(targetSystem.notAvailableMessage)
                        HttpStatus.INTERNAL_SERVER_ERROR -> InternalErrorException(targetSystem.internalErrorMessage)
                        else -> specificCalls(it)
                    }
                }
                is ConnectException -> ServiceIsUnavailableException(targetSystem.notAvailableMessage)
                is UnknownHostException -> ClientUnknownHostException(targetSystem.unknownHostMessage)
                else -> it.also { logger.error("An unhandled exception was thrown: [${it.localizedMessage}] -> [$it]") }
            }
        }
    }

    enum class TargetSystem(
        val notAvailableMessage: String,
        val internalErrorMessage: String,
        val unknownHostMessage: String,
    ) {
        MARKET_STACK(
            "MarketStack is not available in the moment",
            "An unexpected error was thrown in MarketStack",
            "Client was not able to connect to MarketStack"
        )
    }
}
