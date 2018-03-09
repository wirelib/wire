package com.matteojoliveau.wire

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.telegram.ResponseWrapper
import org.telegram.User

class Wire(token: String, private val apiUrl: String = "https://api.telegram.org/bot$token") {
    private val client = httpClient()
    private val gson = Gson()

    private fun httpClient(): OkHttpClient {
        return OkHttpClient()
    }

    fun getMe(): User {
        val request = Request.Builder()
                .url("$apiUrl/getMe")
                .build()
        val response = client.newCall(request).execute()

        val body = response.body()
        val json = body?.string() ?: "invalid"
        val res: ResponseWrapper<User> = gson.fromJson(json)
        return res.result
    }
}

