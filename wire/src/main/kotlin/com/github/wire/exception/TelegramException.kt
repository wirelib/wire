package com.github.wire.exception

import org.telegram.ResponseWrapper


class TelegramException(override val message: String, override val cause: Throwable? = null): Exception(message,cause) {
    companion object {
        infix fun from(wrapper: ResponseWrapper<*>) = TelegramException(wrapper.description
                ?: "Unexpected response: $wrapper")
    }
}