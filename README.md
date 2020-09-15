# Aktien Application

Wenn du schon immer wissen wolltest, wie sich deine Aktien über einen bestimmten Zeitraum entwickelt haben, dann ist diese App genau richtig für dich.
Mit AktienApp kannst du super easy deine Aktie in den application.properties hinzufügen und schon kannst du mit simplen REST aufrufen herausfinden, wie die Entwicklung aussieht.

### Getting Started
| application.property                    | Beschreibung                                                                                                                                                        |
|-----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| marketstack.apiKey                      | Hierzu einfach den ApiKey von MarketStack (https://marketstack.com/) kopieren und den Wert hier einfügen                                                            |
| marketstack.symbols.{aktienSymbol}.name | Ein Aktiensymbol eine Abkürzung zur Identifizierung von börsennotierten Unternehmen. Der von Apple wäre AAPL. Ersetze den Platzhalter {aktienSymbol} mit z. B. AAPL |
| marketstack.symbols.AAPL.name           | Du kannst als Anzeigename einen Wert nach AAPL.name einfügen.                                                                                                       |
| marketstack.symbols.AAPL.purchaseDate   | Hier trägst du das Kaufdatum ein, wann du diese Aktie gekauft hast. Im Format YYYY-MM-DD                                                                            |

