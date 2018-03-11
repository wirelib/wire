package com.github.wire.internal

import com.github.wire.ContextCallback
import com.github.wire.Middleware
import org.telegram.Update

interface UpdateDispatcher {
    fun <T> register(key: T, callback: ContextCallback)
    fun fallback(callback: ContextCallback)
    fun dispatch(update: Update)
    fun dispatch(update: Update, middlewares: List<Middleware>)
}