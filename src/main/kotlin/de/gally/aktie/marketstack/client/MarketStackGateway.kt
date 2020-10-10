package de.gally.aktie.marketstack.client

import de.gally.aktie.BaseWebClient
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate

@Component
@EnableConfigurationProperties(MarketStackGatewayConfiguration::class)
class MarketStackGateway(
    val config: MarketStackGatewayConfiguration,
) : BaseWebClient() {

    /** WebClient for making requests against MarketStack */
    private val client: WebClient by lazy {
        WebClient.builder()
            .baseUrl(config.baseUrl)
            .build()
    }

    /** Get all information for all configured shares */
    suspend fun getDetailedInformation(): Map<String, Map<LocalDate, Double>> {
        val uri = createUri(config.symbols.keys, getOldestPurchaseDate())

        log(TargetSystem.MARKET_STACK, HttpMethod.GET, config.baseUrl + uri)

        val response = client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(MarketStackResponse::class.java)
            .awaitFirst()

        return response.data
            .groupBy { it.symbol }
            .mapValues { (_, detail) -> detail.map { it.date.toLocalDate() to it.close }.toMap() }
            .map { (symbol, data) ->
                val name = config
                    .symbols
                    .getValue(symbol)
                    .name

                val values = data
                    .filterKeys {
                        val purchaseDate = config
                            .symbols
                            .getValue(symbol)
                            .purchaseDate
                            .toLocalDate()

                        val responseDate = it.plusDays(1)

                        purchaseDate.isBefore(responseDate)
                    }

                name to values
            }.toMap()
    }

    /** Get all information for received [symbol] */
    suspend fun getDetailedInformationBySymbol(symbol: String): Map<String, Map<LocalDate, Double>> {
        val uri = createUri(setOf(symbol), config.symbols[symbol]?.purchaseDate)

        log(TargetSystem.MARKET_STACK, HttpMethod.GET, config.baseUrl + uri)

        val response = client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(MarketStackResponse::class.java)
            .awaitFirst()

        return response
            .data
            .groupBy { it.symbol }
            .mapValues { (_, detail) -> detail.map { it.date.toLocalDate() to it.close }.toMap() }
    }

    /** Get a summarized     value of all configured shares */
    suspend fun getTotal(): Map<LocalDate, Double> {
        val uri = createUri(config.symbols.keys, getOldestPurchaseDate())

        log(TargetSystem.MARKET_STACK, HttpMethod.GET, config.baseUrl + uri)

        val response = client.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(MarketStackResponse::class.java)
            .awaitFirst()

        return response.data
            .filter { (symbol, _, date) ->
                val purchaseDate = config
                    .symbols
                    .getValue(symbol)
                    .purchaseDate
                    .toLocalDate()
                val responseDate = date
                    .toLocalDate()
                    .plusDays(1)

                purchaseDate.isBefore(responseDate)
            }
            .map { it.date.toLocalDate() to it.close }
            .groupBy { it.first }
            .mapValues { (_, data) -> data.sumByDouble { it.second } }
    }

    private fun createUri(symbols: Set<String>, dateFrom: String?) = UriComponentsBuilder.newInstance()
        .queryParam("access_key", config.apiKey)
        .queryParam("symbols", symbols.joinToString(separator = ","))
        .queryParam("date_from", dateFrom)
        .build()
        .toUriString()

    private fun getOldestPurchaseDate() = config.symbols
        .values
        .map { LocalDate.parse(it.purchaseDate) }
        .minBy { it }
        .toString()

    private data class MarketStackResponse(val data: List<MarketStackDataResponse>)

    private data class MarketStackDataResponse(val symbol: String, val close: Double, val date: String)

    private fun String.toLocalDate() = substringBefore('T').let { LocalDate.parse(it) }
}
