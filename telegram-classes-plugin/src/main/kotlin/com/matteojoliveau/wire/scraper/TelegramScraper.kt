package com.matteojoliveau.wire.scraper

import org.jsoup.Jsoup

class TelegramScraper {
    val doc = Jsoup.connect("https://core.telegram.org/bots/api").get()

    fun scrape(): List<TemplateModel> {
        val data = mutableSetOf<TemplateModel>()
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
                                val descriptionText = description.text()
                                val entry = mapOf<String, String>("field" to field.text().toCamelCase(), "type" to mapNullable(mapType(type.text()), descriptionText.contains("Optional")), "description" to descriptionText)
                                val parent = field.parent().parent().parent()
                                val title = parent.previousElementSibling()?.previousElementSibling()
                                if ("h4" == title?.tagName()) {
                                    val titleS = title.text().capitalize()
                                    val model = data.find { titleS.equals(it.title, true) }
                                            ?: TemplateModel(titleS, mutableListOf())
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
                                val req = mapRequired(required.text())
                                val entry = mapOf<String, String>("field" to field.text().toCamelCase(), "type" to mapNullable(mapType(type.text()), !req), "required" to req.toString(), "description" to description.text())
                                val parent = field.parent().parent().parent()
                                val title = parent.previousElementSibling()?.previousElementSibling()
                                if ("h4" == title?.tagName()) {
                                    val titleS = title.text().capitalize()
                                    val model = data.find { titleS.equals(it.title, true) }
                                            ?: TemplateModel(titleS, mutableListOf())
                                    model.fields.add(entry)
                                    data.add(model)
                                }
                            }
                        }
                        else -> error("Wrong parameters $it")
                    }
                }


        return data.distinctBy { it.title }.toMutableList()
                .let(::createInputMessageContents)
                .let(::createInlineQueryResults)
                .let(::createInputMedia)
                .let(::createCallbackGame)
    }
}

fun mapRequired(s: String) = "yes".equals(s, true)

fun mapType(s: String): String {
    val arrayOfRegex = Regex("Array of (\\w*)")
    val doubleArrayOfRegex = Regex("Array of Array of (\\w*)")

    return s
            .let {
                when (it) {
                    "Integer" -> "Int"
                    "True" -> "Boolean"
                    "InputFile" -> "java.io.File"
                    else -> it
                }
            }
            .let {
                when {
                    doubleArrayOfRegex.matches(it) -> "List<List<${doubleArrayOfRegex.matchEntire(it)?.groups?.get(1)?.value}>>"
                    arrayOfRegex.matches(it) -> "List<${arrayOfRegex.matchEntire(it)?.groups?.get(1)?.value}>"
                    else -> it
                }
            }
            .let {
                if (it.contains("or")) {
                    val slices = it.split(" or ")
                    slices.lastOrNull() ?: s
                } else {
                    it
                }
            }
            .let {
                if (it.contains("number")) {
                    it.replace("number", "")
                            .replace(" ", "")
                } else {
                    it
                }
            }
}

fun mapNullable(s: String, isNullable: Boolean): String {
    return if (isNullable) "$s?" else s
}

fun createInputMessageContents(data: MutableList<TemplateModel>): MutableList<TemplateModel> {
    val parent = TemplateModel("InputMessageContent", mutableListOf())
    val data2 = data.filter { it.title.startsWith("Input") && it.title.endsWith("Content") }
            .map {
                TemplateModel(it.title, it.fields, parent.title)
            }.toMutableList()
    data2.addAll(data)
    data2.add(parent)
    return data2.distinctBy { it.title }.toMutableList()
}

fun createInlineQueryResults(data: MutableList<TemplateModel>): MutableList<TemplateModel> {
    val parent = TemplateModel("InlineQueryResult", mutableListOf())
    val data2 = data.filter { it.title.startsWith("InlineQueryResult") }
            .map {
                TemplateModel(it.title, it.fields, parent.title)
            }.toMutableList()
    data2.addAll(data)
    data2.add(parent)
    return data2.distinctBy { it.title }.toMutableList()
}

fun createInputMedia(data: MutableList<TemplateModel>): MutableList<TemplateModel> {
    val parent = TemplateModel("InputMedia", mutableListOf())
    val data2 = data.filter { it.title.startsWith("InputMedia") }
            .map {
                TemplateModel(it.title, it.fields, parent.title)
            }.toMutableList()
    data2.addAll(data)
    data2.add(parent)
    return data2.distinctBy { it.title }.toMutableList()
}

fun createCallbackGame(data: MutableList<TemplateModel>): MutableList<TemplateModel>  {
    val callback = TemplateModel("CallbackGame", mutableListOf())
    data.add(callback)
    return data
}




fun String.toCamelCase(): String = "_([a-z\\d])".toRegex().replace(this) {
    it.groups[1]?.value?.toUpperCase() ?: ""
}

fun String.toSnakecase(): String = "[A-Z\\d]".toRegex().replace(this) {
    "_" + it.groups[0]?.value?.toLowerCase()
}