package vgrechka

interface XSharedPlatform {
    fun currentTimeMillis(): Long
    fun getenv(name: String): String?
}

