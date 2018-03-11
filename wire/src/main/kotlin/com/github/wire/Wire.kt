package com.github.wire

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.github.wire.enums.MessageEntityType
import com.github.wire.enums.UpdateType
import com.github.wire.internal.ApiPoller
import com.github.wire.internal.InMemoryDispatcher
import com.github.wire.internal.UpdateDispatcher
import com.github.wire.telegram.Telegram
import io.reactivex.functions.Consumer
import mu.KLogging
import okhttp3.OkHttpClient
import org.telegram.Update

class Wire(token: String, apiUrl: String = "${Constants.TELEGRAM_API_URL}$token") {
    companion object: KLogging()

    private val client = httpClient()
    private val gson = gson()
    private val telegram = Telegram(client, gson, apiUrl, ApiPoller(client, gson, apiUrl))
    private val dispatcher = dispatcher(telegram)
    private val middlewares = mutableListOf<Middleware>()

    private fun httpClient(): OkHttpClient = OkHttpClient()

    private fun gson(): Gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    private fun dispatcher(telegram: Telegram): UpdateDispatcher = InMemoryDispatcher(telegram)

    fun use(vararg m: Middleware) {
        middlewares.addAll(m)
    }
    fun use(middleware: Middleware) {
        middlewares.add(middleware)
    }

    fun onCommand(command: String, callback: ContextCallback): Wire {
        register("/$command", callback)
        return this
    }

    fun on(updateType: UpdateType, callback: ContextCallback): Wire {
        register(updateType, callback)
        return this
    }

    fun onText(regex: Regex, callback: ContextCallback): Wire {
        register(regex, callback)
        return this
    }

    fun onText(regex: String, callback: ContextCallback): Wire = onText(regex.toRegex(), callback)

    fun onMessageEntity(type: MessageEntityType, callback: ContextCallback): Wire {
        register(type, callback)
        return this
    }

    fun onMention(callback: ContextCallback) = onMessageEntity(MessageEntityType.MENTION, callback)

    fun onStartCommand(callback: ContextCallback) = onCommand("start", callback)

    fun onAction(data: String, callback: ContextCallback): Wire {
        register(data, callback)
        return this
    }

    fun onCallbackQuery(callback: ContextCallback): Wire {
        register(UpdateType.CALLBACK_QUERY, callback)
        return this
    }


    fun start(block: Boolean = true) {
        val updates = telegram.start()
        logger.info { "Started polling" }
        if (block) {
            updates.blockingSubscribe(::dispatch, this::onError)
        } else {
            updates.subscribe(::dispatch, this::onError)
        }
    }

    fun catch(handler: Consumer<in Throwable>) {
        telegram.catch(handler)
    }

    fun catch(handler: (t: Throwable) -> Unit) {
        telegram.catch(Consumer {handler(it)})
    }

    private fun dispatch(update: Update) {
        dispatcher.dispatch(update, middlewares)
    }

    private fun onError(t: Throwable) = logger.error(t) { t.message }

    private fun register(key: Any, callback: ContextCallback) = dispatcher.register(key, callback)
}

