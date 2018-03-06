package com.matteojoliveau.wire.scraper

import junit.framework.TestCase

class TelegramScraperTest : TestCase("scraper test") {
    val sut = TelegramScraper()

    fun `testScrape Tables`() {

        sut.scrape()

    }
}