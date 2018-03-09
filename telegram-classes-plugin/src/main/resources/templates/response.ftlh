package org.telegram

data class ResponseWrapper<out T> (
    val ok: Boolean,
    val result: T
    )