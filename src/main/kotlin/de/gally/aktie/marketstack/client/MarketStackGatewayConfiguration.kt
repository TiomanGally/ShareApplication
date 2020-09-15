package de.gally.aktie.marketstack.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "marketstack")
data class MarketStackGatewayConfiguration(
    var baseUrl: String,
    var apiKey: String,
    var symbols: Map<String, Symbol>
) {
    data class Symbol(
        val name: String,
        val purchaseDate: String
    )
}
