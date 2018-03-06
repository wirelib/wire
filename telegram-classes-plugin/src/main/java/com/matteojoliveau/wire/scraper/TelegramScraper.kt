package com.matteojoliveau.wire.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

class TelegramScraper {
    val doc = Jsoup.connect("https://core.telegram.org/bots/api").get()
    fun scrape() {
        val data = mutableListOf<MutableMap<String, Any>>(mutableMapOf())
        val tables = doc.select("table")
        for (table in tables) {
            val rows = table.select("tr")
            for (row in rows) {
                val cols = row.select("td")
                for (col in cols) {
                    if (col.children().filter{"strong" == it.tag().name }.count() > 0) {
                        val child = col.child(0).child(0)
                        print(child)
                    }
                }
            }
        }
    }
}

fun <T> isNotTextNode(t: T): Boolean = t !is TextNode