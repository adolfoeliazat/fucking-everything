package vgrechka.db.tests

import org.hibernate.cfg.Environment
import org.hibernate.dialect.SQLiteDialect
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import vgrechka.*
import vgrechka.db.*
import java.sql.Connection
import java.sql.DriverManager
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

object Spike_SQLite_1 {
    @JvmStatic
    fun main(args: Array<String>) {
        val con = DriverManager.getConnection(BigPile.localSQLiteShebangTestDBURL)
        fuckAroundWithSQLiteConnection(con)
        clog("OK")
    }
}

object Spike_SQLite_DataSource {
    @JvmStatic
    fun main(args: Array<String>) {
        clog("How about DataSource?")
        val ds = SQLiteConnectionPoolDataSource()
        ds.url = BigPile.localSQLiteShebangTestDBURL
        val con = ds.connection
        fuckAroundWithSQLiteConnection(con)
        clog("OK")
    }
}

object Spike_SpringDataJPA {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(DBStuffTestsAppConfig::class.java)

        clog("Fuck you")
    }
}

private fun fuckAroundWithSQLiteConnection(con: Connection) {
    val st = con.prepareStatement("select id, niceWord_word, niceWord_rank from nice_words")
    val rs = st.executeQuery()
    while (rs.next()) {
        clog("ID:", rs.getLong(1))
        clog("Word:", rs.getString(2))
        clog("Rank:", rs.getInt(3))
        clog("------------------------------")
    }
}


interface NiceWordRepository : XCrudRepository<NiceWord, Long>

@XEntity @XTable(name = "nice_words")
data class NiceWord(
    @XEmbedded var niceWord: NiceWordFields
) : ClitoralEntity0()

@XEmbeddable
data class NiceWordFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var word: String,
    @XColumn var rank: Integer
)

private object Spike_SaveShitToSQLiteAndLoadBackViaHibernate_1 {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(DBStuffTestsAppConfig::class.java)

        backPlatform.tx {
            clog("Fucking around?")
            val repo = backPlatform.springctx.getBean(NiceWordRepository::class.java)!!
            val items = repo.findAll()
            for (item in items) {
                clog(item)
            }
        }
        clog("OK")
    }

}

@Suppress("unused")
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@ComponentScan(basePackages = arrayOf("vgrechka.db.tests"))
private open class DBStuffTestsAppConfig {
    @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean() -{o->
        o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
            o.setShowSql(true)
        }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
        o.jpaPropertyMap.put(Environment.DIALECT, SQLiteDialect::class.qualifiedName)
        o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
        o.setPackagesToScan("vgrechka.db.tests")
        o.dataSource = dataSource
    }

    @Bean open fun dataSource(): DataSource {
        return SQLiteConnectionPoolDataSource()-{o->
            o.url = "jdbc:sqlite:e:/febig/db/shebang.db"
        }
    }

    @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
        o.entityManagerFactory = emf
    }
}




