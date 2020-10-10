package de.gally.aktie.marketstack.client

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import strikt.api.expectThat
import strikt.assertions.hasEntry
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "marketstack.baseUrl=http://api.marketstack.com/v1/eod",
        "marketstack.apiKey=SomeApiKey",
        "marketstack.symbols.AAPL.name=Apple Inc.",
        "marketstack.symbols.AAPL.purchaseDate=2020-01-01",
    ]
)
class MarketStackGatewayConfigurationTest {

    @Autowired
    private lateinit var config: MarketStackGatewayConfiguration

    @Test
    fun `assert that all properties are correctly set`() {
        expectThat(config)
            .and {
                get { apiKey }.isEqualTo("SomeApiKey")
                get { baseUrl }.isEqualTo("http://api.marketstack.com/v1/eod")
            }.and {
                get { symbols }
                    .hasSize(1)
                    .hasEntry("AAPL", MarketStackGatewayConfiguration.Symbol("Apple Inc.", "2020-01-01"))
            }
    }
}
