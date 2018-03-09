package com.matteojoliveau.wire.internal

import com.matteojoliveau.wire.ContextCallback
import com.matteojoliveau.wire.telegram.Telegram
import org.telegram.Update

interface UpdateDispatcher {
    fun <T> register(key: T, callback: ContextCallback)
    fun fallback(callback: ContextCallback)
    fun dispatch(update: Update)
}