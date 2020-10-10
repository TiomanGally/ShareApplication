package de.gally.aktie.marketstack

import de.gally.aktie.marketstack.client.MarketStackGateway
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MarketStackController(
    private val gateway: MarketStackGateway,
) {
    @GetMapping("/detail")
    suspend fun getDetailedInformation() =
        gateway.getDetailedInformation()

    @GetMapping("/detail/{symbol}")
    suspend fun getDetailedInformationForShare(@PathVariable symbol: String) =
        gateway.getDetailedInformationBySymbol(symbol)

    @GetMapping("/total")
    suspend fun getTotalInformation() =
        gateway.getTotal()
}
