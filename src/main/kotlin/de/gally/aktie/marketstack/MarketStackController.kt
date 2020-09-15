package de.gally.aktie.marketstack

import de.gally.aktie.marketstack.client.MarketStackGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MarketStackController(
    private val gateway: MarketStackGateway
) {

    @GetMapping("/total")
    fun getTotalInformation() =
        gateway.getTotal().map { ResponseEntity.ok(it) }

    @GetMapping("/detail")
    fun getDetailedInformation() =
        gateway.getDetailedInformation().map { ResponseEntity.ok(it) }

    @GetMapping("/detail/{symbol}")
    fun getDetailedInformationForShare(@PathVariable symbol: String) =
        gateway.getDetailedInformationFor(symbol).map { ResponseEntity.ok(it) }
}
