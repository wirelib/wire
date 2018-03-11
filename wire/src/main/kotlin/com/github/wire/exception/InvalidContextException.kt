package com.github.wire.exception

class InvalidContextException(override val message: String, override val cause: Throwable? = null): Exception(message, cause) {
}