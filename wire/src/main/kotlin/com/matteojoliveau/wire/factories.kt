package com.matteojoliveau.wire

import okhttp3.Request

fun buildUrl(url: String, method: String, params: Map<String, String> = mapOf()): String {
    val sb = StringBuilder("$url/$method")
    params.entries.forEachIndexed { index, entry ->
        sb.append(if (index == 0) "?" else "&")
                .append("${entry.key}=${entry.value}")
    }
    return sb.toString()
}

typealias ContextCallback = (ctx: Context) -> Unit