package com.matteojoliveau.wire

import junit.framework.TestCase

class WireTest : TestCase() {

    val wire = Wire("token")

    fun `testTest GetMe`() {

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

        wire.start()
    }
}