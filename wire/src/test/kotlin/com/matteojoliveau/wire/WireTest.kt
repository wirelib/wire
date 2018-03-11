package com.matteojoliveau.wire

import junit.framework.TestCase
import okhttp3.OkHttpClient
import org.junit.Ignore
import org.telegram.InlineKeyboardButton
import org.telegram.InlineKeyboardMarkup
import org.telegram.ReplyKeyboardMarkup
import java.util.logging.Level
import java.util.logging.Logger

@Ignore
class WireTest : TestCase() {

    val wire = Wire("286303429:AAFj3ImD_5_rXTylf6YfGmVD5Lm_iwu9c6I")

    fun `testTest GetMe`() {
        Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE

        wire.use({ctx, next ->
            val start = System.currentTimeMillis()
            next(ctx)
            val end = System.currentTimeMillis()
            println("Time: ${end - start}ms")
        }, {ctx, next ->
            Thread.sleep(5000)
            next(ctx)
        })

        wire.catch {println("E: ${it.message}")}

        wire.onCommand("pippo") {
            println("Received message: ${it.message?.text} from ${it.from}")
        }

        wire.onText("@\\w*") { ctx ->
            println(ctx.message.toString())
        }
        wire.onMention { ctx ->
            ctx.reply("Ciao ${ctx.mentions.first()}, come stai?").subscribe({ sent ->
                println("Sent $sent")
            }, {error -> println("Error: ${error.message}")})
        }

        wire.onAction("test") {ctx ->
            ctx.replyWithHtml("<code>Le CBQ funzionano!</code>").subscribe( {
                ctx.answerCallbackQuery("Done")
            })
        }

        wire.onCommand("test") {ctx ->
            val markup = InlineKeyboardMarkup(listOf(listOf(InlineKeyboardButton("Test", callbackData = "test"))))
            ctx.replyWithHtml("<b>BRAVO</b>\nEccoti un pulsante", markup)
        }

        wire.start()
    }
}