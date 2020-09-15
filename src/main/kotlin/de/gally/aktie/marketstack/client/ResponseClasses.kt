package de.gally.aktie.marketstack.client

import java.time.LocalDate


data class MarketStackDetailedResponse(
    val close: Double,
    val symbol: String,
    val date: String,
)

data class MarketStackTotalResponse(
    val date: LocalDate,
    val close: Double,
)
