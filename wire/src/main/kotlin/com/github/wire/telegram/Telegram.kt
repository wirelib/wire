package com.github.wire.telegram

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.github.wire.MediaTypes
import com.github.wire.buildUrl
import com.github.wire.enums.ParseMode
import com.github.wire.exception.TelegramException
import com.github.wire.internal.Poller
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import mu.KLogging
import okhttp3.*
import org.telegram.*
import java.util.concurrent.TimeUnit

class Telegram(private val http: OkHttpClient, private val gson: Gson, private val url: String, private val poller: Poller) {
    companion object : KLogging()

    private var errorHandler: Consumer<in Throwable> = Consumer { logger.error(it) { it.message } }

    fun start(): Observable<Update> {
        return Observable.interval(2, TimeUnit.SECONDS)
                .map { poller.poll() }
                .flatMapIterable { it }
    }

    fun catchError(handler: Consumer<in Throwable>) {
        errorHandler = handler
    }

    fun getMe(): Observable<User> {
        return createObs(http.newCall(requestBuilder("getMe").build()))

    }

    fun sendMessage(chatId: String, text: String,
                    parseMode: ParseMode? = null,
                    disableWebPagePreview: Boolean? = null,
                    disableNotification: Boolean? = null,
                    replyToMessageId: Int? = null,
                    replyMarkup: ReplyMarkup? = null
    ): Observable<Message> {
        val payload = SendMessage(chatId, text, parseMode?.name, disableWebPagePreview, disableNotification, replyToMessageId, replyMarkup)
        return createObs("sendMessage" send payload)
    }

    fun forwardMessage(chatId: String, fromChatId: String, messageId: Int, disableNotification: Boolean? = null): Observable<Message> {
        val payload = ForwardMessage(chatId, fromChatId, disableNotification, messageId)
        return createObs("forwardMessage" send payload)
    }

    fun answerCallbackQuery(callbackQueryId: String, text: String? = null, showAlert: Boolean? = null, url: String? = null, cacheTime: Int? = null): Observable<Boolean> {
        val payload = AnswerCallbackQuery(callbackQueryId, text, showAlert, url, cacheTime)
        return createObs("answerCallbackQuery" send payload)
    }

    fun getChat(chatId: String): Observable<Chat> {
        val payload = GetChat(chatId)
        return createObs("getChat" send payload)
    }

    fun editMessageText(text: String, chatId: String? = null, messageId: Int? = null, inlineMessageId: String? = null, parseMode: ParseMode? = null, disableWebPagePreview: Boolean? = null, replyMarkup: InlineKeyboardMarkup): Observable<Message> {
        val payload = EditMessageText(chatId, messageId, inlineMessageId, text, parseMode?.name?.toLowerCase(), disableWebPagePreview, replyMarkup)
        return createObs("editMessageText" send payload)
    }

    private infix fun String.send(body: Any): Call {
        val json = gson.toJson(body)
        val request = requestBuilder(this).post(RequestBody.create(MediaTypes.JSON, json)).build()
        return http.newCall(request)
    }

    private fun requestBuilder(method: String, parameters: Map<String, String> = mapOf()) = Request.Builder().url(buildUrl(url, method, parameters))
    private fun bodyToString(res: Response): String? {
        val body = res.body()
        val json = body?.string()
        res.close()
        return json
    }

    private fun <T> createObs(call: Call): Observable<T> = Observable.just(call.execute()).map(this::bodyToString).map { fromJson<T>(it) }.map { unwrap(it) }.doOnError(errorHandler)

    private fun throwNull(it: Any): Nothing = throw NullPointerException("Response was ok but no result received. $it")

    private fun <T> fromJson(json: String) = gson.fromJson<ResponseWrapper<T>>(json)
    private fun <T> unwrap(w: ResponseWrapper<T>): T = if (w.ok) {
        w.result ?: throwNull(w)
    } else throw TelegramException from w
}
