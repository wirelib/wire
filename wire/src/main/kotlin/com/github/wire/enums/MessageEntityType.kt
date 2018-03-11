package com.github.wire.enums

enum class MessageEntityType {
    MENTION,
    HASHTAG,
    BOT_COMMAND,
    URL,
    EMAIL,
    BOLD,
    ITALIC,
    CODE,
    PRE,
    TEXT_LINK,
    TEXT_MENTION
    ;

    fun equals(string: String): Boolean = this.name.equals(string, true)

}