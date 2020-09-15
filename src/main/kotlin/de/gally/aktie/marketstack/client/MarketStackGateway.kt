package de.gally.aktie.marketstack.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.gally.aktie.BaseWebClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
@EnableConfigurationProperties(MarketStackGatewayConfiguration::class)
class MarketStackGateway(
    val config: MarketStackGatewayConfiguration,
) : BaseWebClient() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /** WebClient for making requests against MarketStack */
    private val client: WebClient by lazy {
        WebClient.builder()
            .baseUrl(config.baseUrl)
            .build()
    }

    /** Get for each day after purchased date the close value  of configured shares */
    fun getDetailedInformation(): Mono<Map<String, List<MarketStackTotalResponse>>> {
        val uri = UriComponentsBuilder.newInstance()
            .queryParam("access_key", config.apiKey)
            .queryParam("symbols", config.symbols.keys.joinToString(separator = ","))
            .queryParam("date_from", getOldestPurchaseDate())
            .build().toUriString()

        log(TargetSystem.MARKET_STACK, HttpMethod.GET, config.baseUrl + uri)

        return client
            .get()
            .uri(uri)
            .retrieveDetailedResponse()
            .handleClientError(TargetSystem.MARKET_STACK)
    }

    /** Get detailed share information of [symbol] */
    fun getDetailedInformationFor(symbol: String): Mono<Map<String, List<MarketStackTotalResponse>>> {
        val uri = UriComponentsBuilder.newInstance()
            .queryParam("access_key", config.apiKey)
            .queryParam("symbols", symbol)
            .queryParam("date_from", config.symbols[symbol]?.purchaseDate)
            .build().toUriString()

        log(TargetSystem.MARKET_STACK, HttpMethod.GET, config.baseUrl + uri)

        return client
            .get()
            .uri(uri)
            .retrieveDetailedResponse()
            .handleClientError(TargetSystem.MARKET_STACK)
    }

    /** Get the total close value of each day */
    fun getTotal() = getDetailedInformation()
        .map { it.values.flatten() }
        .map { allCloseValues ->
            allCloseValues
                .groupBy { it.date }
                .mapValues { (_, allCloseValues) ->
                    allCloseValues.sumByDouble { it.close }
                }
        }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class TotalResponse(val data: List<TotalInfo>)

    private data class TotalInfo(val close: Double, val symbol: String, val date: String)

    private fun getOldestPurchaseDate() = config.symbols
        .values
        .map { LocalDate.parse(it.purchaseDate) }
        .minBy { it }
        ?.toString()

    private fun String.toLocalDate() = LocalDate.parse(this.substringBefore('T'))

    private fun WebClient.RequestHeadersSpec<*>.retrieveDetailedResponse() = retrieve()
        .bodyToMono(TotalResponse::class.java)
        .doOnSuccess { logger.info("Received Response: $it") }
        .map { response -> response.data.map { MarketStackDetailedResponse(it.close, it.symbol, it.date) } }
        .map { allFilteredMarketStackDetailedResponses ->
            val response = mutableMapOf<String, MutableList<MarketStackTotalResponse>>()

            allFilteredMarketStackDetailedResponses.map {
                response
                    .getOrPut(it.symbol, { mutableListOf() })
                    .add(MarketStackTotalResponse(it.date.toLocalDate(), it.close))
            }

            return@map response
                .toMap()
                .mapValues { (_, list) -> list.toList() }
        }
}
