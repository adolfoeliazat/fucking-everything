package aps

import aps.back.*

@Back open class RequestMatumba {
    val _fields = mutableListOf<FormFieldBack>()
    @Dummy fun beginHorizontal() {}
    @Dummy fun endHorizontal() {}
}

fun t(en: String, ru: String) = ru

@Dummy interface XIcon
@Dummy class Twemoji(val hex: String) : XIcon
@Dummy class EmojiOne(val hex: String) : XIcon

