package vgrechka

val sharedPlatform = object : XSharedPlatform {
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun getenv(name: String): String? = System.getenv(name)
}
