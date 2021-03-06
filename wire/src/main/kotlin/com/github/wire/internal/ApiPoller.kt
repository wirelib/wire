package com.github.wire.internal

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.github.wire.buildUrl
import com.github.wire.exception.TelegramException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.telegram.ResponseWrapper
import org.telegram.Update

class ApiPoller(private val http: OkHttpClient, private val gson: Gson, private val url: String) : Poller {
    private var lastOffset = 0
    override fun poll(): Iterable<Update> {
        val params = mutableMapOf("timeout" to 1.toString(), "offset" to lastOffset.toString())
        val request = requestBuilder("getUpdates", params).build()
        val response = http.newCall(request).execute()
        val body = response.body()
        val json = body?.string() ?: "{}"
        response.close()
        val wrapper: ResponseWrapper<Array<Update>> = gson.fromJson(json)
        return if (wrapper.ok) {
            val updates = wrapper.result?.toList() ?: listOf()
            updates.forEach {
                lastOffset = it.updateId + 1
            }
            updates
        } else {
            throw TelegramException from wrapper
        }
    }

    private fun requestBuilder(method: String, parameters: Map<String, String> = mapOf()) = Request.Builder().url(buildUrl(url, method, parameters))

}