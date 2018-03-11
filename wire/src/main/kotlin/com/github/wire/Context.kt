package com.github.wire

import com.github.wire.enums.ParseMode
import com.github.wire.enums.UpdateType
import com.github.wire.telegram.Telegram
import org.telegram.InlineQuery
import org.telegram.Message
import org.telegram.ReplyMarkup
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

    private val custom = mutableMapOf<String, Any>()

    val mentions: List<String>
        get() = message?.entities?.map { message.text?.substring(it.offset, it.offset + it.length) ?: "" }
                ?: listOf()

    @JvmOverloads fun reply(text: String, replyMarkup: ReplyMarkup? = null,
              disableWebPagePreview: Boolean? = null,
              disableNotification: Boolean? = null,
              replyToMessageId: Int? = null) = telegram.sendMessage(
            chat?.id?.toString() ?: callbackQuery?.message?.chat?.id?.toString() ?: callbackQuery?.chatInstance
            ?: throwInvalid("chat"),
            text, null, disableWebPagePreview, disableNotification, replyToMessageId, replyMarkup)

    @JvmOverloads fun replyWithHtml(text: String, replyMarkup: ReplyMarkup? = null,
                      disableWebPagePreview: Boolean? = null,
                      disableNotification: Boolean? = null,
                      replyToMessageId: Int? = null) = telegram.sendMessage(
            chat?.id?.toString() ?: callbackQuery?.message?.chat?.id?.toString() ?: callbackQuery?.chatInstance
            ?: throwInvalid("chat"),
            text, ParseMode.HTML, disableWebPagePreview, disableNotification, replyToMessageId, replyMarkup)

    @JvmOverloads fun answerCallbackQuery(text: String? = null, showAlert: Boolean? = null, url: String? = null, cacheTime: Int? = null) = telegram.answerCallbackQuery(update.callbackQuery?.id
            ?: throwInvalid("callback query"), text, showAlert, url, cacheTime)

    operator fun set(key: String, value: Any) {
        custom[key] = value
    }
    operator fun get(key: String) = custom[key]

}
