package vgrechka

object BigPile {
    val fuckingEverythingSmallRoot = "e:/fegh"
    val fuckingEverythingBigRoot = "e:/febig"
    val fuckingBackupsRoot = "c:/tmp/fucking-backups"

    val shitForTestsSmallRoot = fuckingEverythingSmallRoot + "/shit-for-tests"
    val shitForTestsBigRoot = fuckingEverythingBigRoot + "/shit-for-tests"

    val localSQLiteShebangDBFilePath = fuckingEverythingBigRoot + "/db/shebang.db"
    val localSQLiteShebangDBURL = "jdbc:intosqlite:$localSQLiteShebangDBFilePath"

    val localSQLiteShebangTestDBFilePath = fuckingEverythingBigRoot + "/db/shebang-test.db"
    val localSQLiteShebangTestDBURL = "jdbc:intosqlite:$localSQLiteShebangTestDBFilePath"


    fun mangleUUID(uuid: String): String {
        return uuid[0] + "-" + uuid.drop(1)
    }

    fun isRunningFromIntelliJ(): Boolean {
        val classPath = System.getProperty("java.class.path")
        return classPath.contains("idea_rt.jar")
    }
}

