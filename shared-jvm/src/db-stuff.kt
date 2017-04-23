package vgrechka

import java.sql.DriverManager

object Spike_SQLite_1 {
    @JvmStatic
    fun main(args: Array<String>) {
        val con = DriverManager.getConnection("jdbc:sqlite:e:/febig/db/shebang.db")
        val st = con.prepareStatement("select id, word, rank from nice_words")
        val rs = st.executeQuery()
        while (rs.next()) {
            clog("ID:", rs.getLong(1))
            clog("Word:", rs.getString(2))
            clog("Rank:", rs.getInt(3))
            clog("------------------------------")
        }
        clog("OK")
    }
}