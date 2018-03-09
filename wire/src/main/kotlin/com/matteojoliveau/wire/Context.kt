package com.matteojoliveau.wire

import com.matteojoliveau.wire.enums.UpdateType
import com.matteojoliveau.wire.exception.InvalidContextException
import com.matteojoliveau.wire.telegram.Telegram
import org.telegram.InlineQuery
import org.telegram.Message
import org.telegram.Update

data class Context(
        val update: Update,
        val telegram: Telegram,
        val updateType: UpdateType
) {
    val message: Message? = update.message
    val editedMessage: Message? = update.editedMessage
    val inlineQuery: InlineQuery? = update.inlineQuery
    val chosenInlineResult = update.chosenInlineResult
    val callbackQuery = update.callbackQuery
    val shippingQuery = update.shippingQuery
    val preCheckoutQuery = update.preCheckoutQuery
    val channelPost = update.channelPost
    val editedChannelPost = update.editedChannelPost
    val chat = update.message?.chat
    val from = update.message?.from

    val mentions: List<String>
        get() = message?.entities?.map { message.text?.substring(it.offset, it.offset + it.length) ?: "" }
                ?: listOf()

    fun reply(text: String) = telegram.sendMessage(chat?.id?.toString() ?: throw InvalidContextException("Cannot reply without a chat object"), text)
}