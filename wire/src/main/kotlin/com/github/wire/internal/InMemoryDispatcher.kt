package com.github.wire.internal

import com.github.wire.Context
import com.github.wire.ContextCallback
import com.github.wire.Middleware
import com.github.wire.enums.MessageEntityType
import com.github.wire.enums.UpdateType
import com.github.wire.telegram.Telegram
import mu.KLogging
import org.telegram.Update
import java.util.regex.Pattern

class InMemoryDispatcher(val telegram: Telegram) : UpdateDispatcher {
    companion object : KLogging()

    private val updateTypes = mutableMapOf<UpdateType, ContextCallback>()
    private val commands = mutableMapOf<String, ContextCallback>()
    private val actions = mutableMapOf<String, ContextCallback>()
    private val textListeners = mutableMapOf<Regex, ContextCallback>()
    private val entities = mutableMapOf<MessageEntityType, ContextCallback>()

    private var fallback: ContextCallback = ContextCallback { logger.debug { "Fallback - $it" } }

    override fun <T> register(key: T, callback: ContextCallback) {
        when (key) {
            is UpdateType -> updateTypes[key] = callback
            is Regex -> textListeners[key] = callback
            is Pattern -> register(key.toRegex(), callback)
            is String -> registerString(key, callback)
            is MessageEntityType -> entities[key] = callback
        }
    }

    private fun registerString(key: String, callback: ContextCallback) {
        when {
            key.startsWith("/") -> commands[key] = callback
            else -> actions[key] = callback
        }
    }

    override fun fallback(callback: ContextCallback) {
        fallback = callback
    }

    override fun dispatch(update: Update) = dispatch(update, listOf())

    override fun dispatch(update: Update, middlewares: List<Middleware>) {
        val ctx = Context(update, telegram, mapUpdateType(update))
        runMiddlewares(ctx, middlewares)
        when (ctx.updateType) {
            UpdateType.MESSAGE -> dispatchMessage(ctx)
            UpdateType.CALLBACK_QUERY -> {
                (actions[update.callbackQuery?.data] ?: updateTypes[UpdateType.CALLBACK_QUERY] ?: fallback).run(ctx)
            }
            else -> fallback.run(ctx)
        }
    }

    private fun dispatchMessage(ctx: Context) {
        val message = ctx.update.message
        if (message != null) {
            if (message.entities?.isEmpty() == false) {
                dispatchEntities(ctx)
            }
            val botCommands = message.entities?.filter { MessageEntityType.BOT_COMMAND.name.equals(it.type, true) }
                    ?: listOf()
            val text = message.text ?: ""
            if (commands.isNotEmpty() && botCommands.isNotEmpty()) {
                val (_, offset, length, _, _) = botCommands[0]
                (commands[text.substring(offset, offset + length)] ?: fallback).run(ctx)
            } else {
                (textListeners.entries.firstOrNull { it.key.matches(text) }?.value ?: fallback).run(ctx)
            }
        }
    }

    private fun dispatchEntities(ctx: Context) {
        ctx.message?.entities?.forEach {
            (entities[MessageEntityType.valueOf(it.type.toUpperCase())] ?: fallback).run(ctx)
        }
    }

    private fun mapUpdateType(update: Update): UpdateType = when {
        update.message != null -> UpdateType.MESSAGE
        update.editedMessage != null -> UpdateType.EDITED_MESSAGE
        update.inlineQuery != null -> UpdateType.INLINE_QUERY
        update.chosenInlineResult != null -> UpdateType.CHOSEN_INLINE_RESULT
        update.callbackQuery != null -> UpdateType.CALLBACK_QUERY
        update.shippingQuery != null -> UpdateType.SHIPPING_QUERY
        update.preCheckoutQuery != null -> UpdateType.PRE_CHECKOUT_QUERY
        update.channelPost != null -> UpdateType.CHANNEL_POST
        update.editedChannelPost != null -> UpdateType.EDITED_CHANNEL_POST
        else -> UpdateType.UNKNOWN
    }

    private fun runMiddlewares(context: Context, middlewares: List<Middleware>) {
        var index = -1
        if (middlewares.isEmpty()) return

        fun execute(i: Int, ctx: Context) {
            if (i <= index) TODO()
            if (middlewares.isEmpty()) return
            if (middlewares.size == 1)  middlewares[0].run(context) {}
            index = i
            if (middlewares.size >= i +1) {
                val middleware = middlewares[i]
                middleware.run(context) { c -> execute(i + 1, c ?: ctx) }
            }
        }

        execute(0, context)
    }



}