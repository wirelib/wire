package com.matteojoliveau.wire

import junit.framework.TestCase

class WireTest : TestCase() {

    val wire = Wire("286303429:AAFj3ImD_5_rXTylf6YfGmVD5Lm_iwu9c6I")

    fun `testTest GetMe`() {
        val me = wire.getMe()
        println(me)
    }
}