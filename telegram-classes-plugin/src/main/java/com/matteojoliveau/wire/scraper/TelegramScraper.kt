package com.matteojoliveau.wire.scraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

class TelegramScraper {
    val doc = Jsoup.connect("https://core.telegram.org/bots/api").get()

    fun scrape(): List<TemplateModel> {
        val data = mutableListOf<TemplateModel>()
        val tables = doc.select("table")
        tables.map { it.select("tr") }
                .flatMap { it }
                .map { it.select("td") }
                .forEach {
                    when (it.size) {
                        3 -> {
                            val field = it[0]
                            val type = it[1]
                            val description = it[2]



                            if ("strong" != field.children().first()?.tagName()) {
                                val entry = mapOf<String, String>("field" to field.text().toCamelCase(), "type" to mapType(type.text()), "description" to description.text())
                                val parent = field.parent().parent().parent()
                                val title = parent.previousElementSibling()?.previousElementSibling()
                                if ("h4" == title?.tagName()) {
                                    val model = data.find { title.text() == it.title }
                                            ?: TemplateModel(title.text().capitalize(), mutableListOf())
                                    model.fields.add(entry)
                                    data.add(model)
                                }
                            }
                        }
                        4 -> {
                            val field = it[0]
                            val type = it[1]
                            val required = it[2]
                            val description = it[3]

                            if ("strong" != field.children().first()?.tagName()) {
                                val entry = mapOf<String, String>("field" to field.text().toCamelCase(), "type" to mapType(type.text()), "required" to mapRequired(required.text()).toString(), "description" to description.text())
                                val parent = field.parent().parent().parent()
                                val title = parent.previousElementSibling()?.previousElementSibling()
                                if ("h4" == title?.tagName()) {
                                    val model = data.find { title.text() == it.title }
                                            ?: TemplateModel(title.text().capitalize(), mutableListOf())
                                    model.fields.add(entry)
                                    data.add(model)
                                }
                            }
                        }
                        else -> error("Wrong parameters $it")
                    }
                }
        return data
    }
}

fun mapRequired(s: String) = "yes".equals(s, true)

fun mapType(s: String): String {
    val arrayOfRegex = Regex("Array of (\\w*)")
    val doubleArrayOfRegex = Regex("Array of Array of (\\w*)")
    return when {
        "Integer" == s -> "Int"
        doubleArrayOfRegex.matches(s) -> "List<List<${doubleArrayOfRegex.matchEntire(s)?.groups?.get(1)?.value}>>"
        arrayOfRegex.matches(s) -> "List<${arrayOfRegex.matchEntire(s)?.groups?.get(1)?.value}>"
        else -> s
    }
}


fun String.toCamelCase(): String = "_([a-z\\d])".toRegex().replace(this){
    it.groups[1]?.value?.toUpperCase() ?: ""
}

fun String.toSnakecase(): String = "[A-Z\\d]".toRegex().replace(this) {
    "_" + it.groups[0]?.value?.toLowerCase()
}