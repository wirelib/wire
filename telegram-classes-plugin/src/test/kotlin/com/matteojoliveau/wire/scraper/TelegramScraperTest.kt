package com.matteojoliveau.wire.scraper

import com.google.gson.GsonBuilder
import freemarker.core.UndefinedOutputFormat
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import junit.framework.TestCase
import java.io.File
import kotlin.text.Charsets.UTF_8


class TelegramScraperTest : TestCase("com.matteojoliveau.wire.scraper test") {
    val sut = TelegramScraper()
    lateinit var cfg: Configuration

    override fun setUp() {
        cfg = Configuration(Configuration.VERSION_2_3_27)
        cfg.setDirectoryForTemplateLoading(javaClass.getResource("/templates").file.toFile())
        cfg.wrapUncheckedExceptions = true
        cfg.logTemplateExceptions = false
        cfg.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        cfg.defaultEncoding = "UTF-8"
        cfg.outputFormat = UndefinedOutputFormat.INSTANCE
    }

    fun `testScrape Tables`() {
        val template = cfg.getTemplate("test.ftlh")
        val data = sut.scrape()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(data)
        File("C:\\Users\\matte\\Desktop\\telegram.json").printWriter().use { out ->
            out.print(json)
        }
        val basePath = "C:\\Users\\matte\\software\\wire-lib\\telegram-classes-plugin\\target\\generated-sources\\telegram"
        val dir = basePath.toFile()
        val success = dir.mkdirs()
        val packa = File(dir, "org/telegram")
        val mkdirs = packa.mkdirs()
        data.forEach {
            val filePath = "${packa.absolutePath}\\${it.title}.kt"

            filePath.toFile().printWriter(UTF_8).use { out ->
                template.process(it, out)
            }
        }
    }

    fun `testShould Scrape Some Data`() {
        val data = sut.scrape()
        assertFalse(data.isEmpty())
    }

    fun String.toFile() = File(this)

}