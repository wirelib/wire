package com.matteojoliveau.wire.internal

import org.telegram.Update

interface Poller {
    fun poll(): Iterable<Update>
}