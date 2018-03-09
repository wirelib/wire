package com.matteojoliveau.wire.telegram

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.matteojoliveau.wire.buildUrl
import com.matteojoliveau.wire.enums.MediaTypes
import com.matteojoliveau.wire.internal.Poller
import io.reactivex.Flowable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.telegram.*
import java.util.concurrent.TimeUnit

class Telegram(val http: OkHttpClient, private val gson: Gson, val url: String, private val poller: Poller) {

    fun start(): Observable<Update> {
        return Observable.interval(2, TimeUnit.SECONDS)
                .map { poller.poll() }
                .flatMapIterable { it }
    }

    fun getMe(): Flowable<ResponseWrapper<User>> {
        return Flowable.fromCallable { http.newCall(requestBuilder("getMe").build()).execute() }

                .map(this::bodyToString)
                .map { gson.fromJson<ResponseWrapper<User>>(it) }

    }

    fun sendMessage(chatId: String, text: String,
                    parseMode: String? = null,
                    disableWebPagePreview: Boolean? = null,
                    disableNotification: Boolean? = null,
                    replyToMessageId: Int? = null,
                    replyMarkup: ForceReply? = null
    ): Observable<Message> {
        val payload = SendMessage(
                chatId,
                text,
                parseMode,
                disableWebPagePreview,
                disableNotification,
                replyToMessageId,
                replyMarkup
        )
        val json = gson.toJson(payload)
        val req = requestBuilder("sendMessage").post(RequestBody.create(MediaTypes.JSON, json)).build()
        return Observable.just(http.newCall(req).execute()).map(this::bodyToString).map { gson.fromJson<ResponseWrapper<Message>>(it) }.map {
            val message = unwrap(it)
            message ?: throw NullPointerException("Response was ok but no message received. $it")
        }
    }

    private fun requestBuilder(method: String, parameters: Map<String, String> = mapOf()) = Request.Builder().url(buildUrl(url, method, parameters))
    private fun bodyToString(res: Response) = res.body()?.string()
    private fun <T> unwrap(wrapper: ResponseWrapper<T>) = if (wrapper.ok) wrapper.result else null
}
