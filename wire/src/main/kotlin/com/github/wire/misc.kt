package com.github.wire

import com.github.wire.exception.InvalidContextException

fun buildUrl(url: String, method: String, params: Map<String, String> = mapOf()): String {
    val sb = StringBuilder("$url/$method")
    params.entries.forEachIndexed { index, entry ->
        sb.append(if (index == 0) "?" else "&")
                .append("${entry.key}=${entry.value}")
    }
    return sb.toString()
}

fun throwInvalid(obj: String): Nothing = throw InvalidContextException("Cannot reply without a $obj object")
