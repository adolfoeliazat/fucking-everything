@file:GSpit(spewClassName = "vgrechka.spew.OptsBuilderSpew", output = "%FE%/db-stuff/gen/generated--db-stuff.kt")

package vgrechka.db

import com.zaxxer.hikari.HikariDataSource
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder
import org.fusesource.jansi.AnsiConsole
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl
import org.hibernate.boot.model.source.spi.AttributePath
import org.hibernate.cfg.Environment
import org.hibernate.dialect.SQLiteDialect
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentityGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaContext
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.sqlite.SQLiteConfig
import org.sqlite.javax.IntoSQLiteConnectionPoolDataSource
import vgrechka.*
import vgrechka.spew.*
import java.sql.Connection
import java.sql.ResultSetMetaData
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import kotlin.reflect.KClass

class ResultSetColumnMeta(val meta: ResultSetMetaData, val index1: Int) {
    val name get() = meta.getColumnName(index1)!!
}

object DBPile {
    fun executeBunchOfSQLStatementsAndCloseConnection(sql: String) {
        val sqls = sql.split(";").filter {it.isNotBlank()}
        executeBunchOfSQLStatementsAndCloseConnection(sqls)
    }

    fun executeBunchOfSQLStatementsAndCloseConnection(sqls: List<String>) {
        withConnection {con->
            val st = con.createStatement()
            for (sql in sqls) {
                st.execute(sql)
            }
        }
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        val ds = backPlatform.springctx.getBean(DataSource::class.java)
        val con = ds.connection
        try {
            return block(con)
        } finally {
            con.close()
        }
    }

    val jpaContext get() = backPlatform.springctx.getBean(JpaContext::class.java)!!

    fun pg_dump(dbConnectionParams: DBConnectionParams, toFile: String) {
        val p = dbConnectionParams
        val passwordPart = when (p.password) {
            null -> ""
            else -> ":${p.password}"
        }
        val cmdPieces = listOf(
            BigPile.saucerfulOfSecrets.pg_dump,
            "--dbname=postgresql://${p.user}$passwordPart@${p.host}:${p.port}/${p.dbName}",
            "--clean",
            "--file=$toFile"
        )
        val res = BigPile.runProcessAndWait(cmdPieces)
        if (res.exitValue != 0)
            bitch("pg_dump said us 'Fuck you'")
    }

}

class NiceHibernateNamingStrategy : ImplicitNamingStrategyJpaCompliantImpl() {
    override fun transformAttributePath(attributePath: AttributePath): String {
        return attributePath.fullPath.replace(".", "_")
    }

    override fun determineJoinColumnName(source: ImplicitJoinColumnNameSource): Identifier {
        val name: String
        if (source.nature == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION || source.attributePath == null ) {
            name = transformEntityName(source.entityNaming) + "__" + source.referencedColumnName.text
        } else {
            name = transformAttributePath(source.attributePath) + "__" + source.referencedColumnName.text
        }
        return toIdentifier(name, source.buildingContext)
    }
}

interface VaginalEntity {
    val id: Long?
    var imposedIDToGenerate: Long?
}

@XMappedSuperclass
abstract class ClitoralEntity0 {
    @XId
    @XGeneratedValue(strategy = XGenerationType.IDENTITY, generator = "IdentityIfNotSetGenerator")
    @XGenericGenerator(name = "IdentityIfNotSetGenerator", strategy = "vgrechka.db.IdentityIfNotSetGenerator")
    var id: Long? = null

    @XTransient
    var imposedIDToGenerate: Long? = null

    @XPreUpdate
    fun preFuckingUpdate() {
//        if (backPlatform.isRequestThread() && !backPlatform.requestGlobus.shitIsDangerous) {
//            if (this is User) {
//                saveUserParamsHistory(this)
//            }
//        }
    }
}

@Suppress("Unused")
class IdentityIfNotSetGenerator : IdentityGenerator() {
    private val logic = IdentityIfNotSetGeneratorLogic()

    override fun generate(s: SharedSessionContractImplementor?, obj: Any?): XSerializable {
        val id = logic.generate(obj)
        return when {
            id != null -> id
            else -> super.generate(s, obj)
        }
    }
}

@Suppress("Unused")
class IdentityIfNotSetGenerator2 : IdentityGenerator() {
    private val logic = IdentityIfNotSetGeneratorLogic2()

    override fun generate(s: SharedSessionContractImplementor?, obj: Any?): XSerializable {
        val id = logic.generate(obj)
        return when {
            id != null -> id
            else -> super.generate(s, obj)
        }
    }
}

// TODO:vgrechka Why the fuck did I need this to be in a separate class?
class IdentityIfNotSetGeneratorLogic {
    /**
     * @return null if default identity generator should be used
     */
    fun generate(obj: Any?): Long? {
        val entity = obj as ClitoralEntity0
        val id = entity.id
        val imposedIDToGenerate = entity.imposedIDToGenerate
        return when {
            id != null -> id
            imposedIDToGenerate != null -> imposedIDToGenerate
            else -> null
        }
    }
}

class IdentityIfNotSetGeneratorLogic2 {
    /**
     * @return null if default identity generator should be used
     */
    fun generate(obj: Any?): Long? {
        val entity = obj as VaginalEntity
        val id = entity.id
        val imposedIDToGenerate = entity.imposedIDToGenerate
        return when {
            id != null -> id
            imposedIDToGenerate != null -> imposedIDToGenerate
            else -> null
        }
    }
}

private fun currentTimestampForEntity(): XTimestamp {
    return when {
//        backPlatform.isRequestThread() -> backPlatform.requestGlobus.stamp
        else -> XTimestamp(sharedPlatform.currentTimeMillis())
    }
}

@XEmbeddable
data class CommonFields(
    var createdAt: XTimestamp = currentTimestampForEntity(),
    var updatedAt: XTimestamp = createdAt,
    var deleted: Boolean = false
) {
//    fun touch() {
//        updatedAt = backPlatform.requestGlobus.stamp
//    }
}

@Configuration
@EnableTransactionManagement
abstract class BaseAppConfig(val entityPackagesToScan: Array<String>) {

    data class Settings(
        val colorfulConsole: Boolean
    )

    data class DataProxySourceSettings(
        val colorful: Boolean
    )

    @Bean open fun initShit(settings: Settings): Any {
        if (settings.colorfulConsole) {
            if (!BigPile.isRunningFromIntelliJ()) {
                AnsiConsole.systemInstall()
            }
        }

        return Any()
    }

    @Bean open fun settings() = Settings(
        colorfulConsole = true
    )

    @Bean open fun dataSourceProxySettings() = DataProxySourceSettings(
        colorful = true
    )

    protected abstract val hibernateDialect: KClass<*>?

    @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean()-{o->
        o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
            //            o.setShowSql(true)
        }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
        hibernateDialect?.let {
            o.jpaPropertyMap.put(Environment.DIALECT, it.qualifiedName)
        }
        o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
        o.setPackagesToScan(*entityPackagesToScan)
        o.dataSource = dataSource
    }

    @Bean open fun dataSource(): DataSource {
        val ds = makeDataSource()
        return ProxyDataSourceBuilder()
            .dataSource(ds)
            .listener(SLF4JQueryLoggingListener()-{o->
                o.logLevel = SLF4JLogLevel.INFO
                o.queryLogEntryCreator = FreakingQueryLogEntryCreator()-{o->
                    o.isMultiline = true
                }
            })
            .build()
    }

    protected abstract fun makeDataSource(): DataSource

    @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
        o.entityManagerFactory = emf
    }
}

abstract class BaseSQLiteAppConfig(entityPackagesToScan: Array<String>) : BaseAppConfig(entityPackagesToScan) {
    abstract val databaseURL: String

    override val hibernateDialect = SQLiteDialect::class

    override fun makeDataSource(): DataSource {
        return IntoSQLiteConnectionPoolDataSource()-{o->
            o.url = databaseURL
            o.config = SQLiteConfig()-{o->
                o.setDateClass(SQLiteConfig.DateClass.TEXT.value)
                o.enforceForeignKeys(true)
            }
        }
    }
}

abstract class BasePostgresAppConfig(entityPackagesToScan: Array<String>) : BaseAppConfig(entityPackagesToScan) {
    abstract val dbConnectionParams: DBConnectionParams

    override val hibernateDialect = null

    override fun makeDataSource(): DataSource {
        return HikariDataSource()-{o->
            o.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            o.dataSourceProperties.let {o->
                o.put("serverName", dbConnectionParams.host)
                o.put("portNumber", dbConnectionParams.port)
                o.put("databaseName", dbConnectionParams.dbName)
                o.put("user", dbConnectionParams.user)
                dbConnectionParams.password?.let {o.put("password", it)}
            }
            o.connectionInitSql = "set time zone 'UTC';"
        }
    }
}

abstract class BaseTestSQLiteAppConfig(entityPackagesToScan: Array<String>): BaseSQLiteAppConfig(entityPackagesToScan) {
    override val databaseURL = BigPile.localSQLiteShebangTestDBURL
}

@GOptsBuilder
class ExecuteAndFormatResultForPrinting : Generated_BaseFor_ExecuteAndFormatResultForPrinting() {
    class Opts(
        val sql: String,
        val title: Title = Title.None(),
        val skipColumn: (ResultSetColumnMeta) -> Boolean = {false},
        val appender: (Ignition) -> Appender
    )

    sealed class Title {
        class None : Title()
        class Explicit(val value: String) : Title()
        class AutoDetect : Title()
        class SQL: Title()
    }

    interface Appender {
        fun appendColumn(renderedColumnIndex0: Int, tableColumnIndex1: Int, value: Any?)
        fun afterAllColumnsInRow()
    }

    fun linePerColumn() = appender({g-> object : Appender {
        override fun appendColumn(renderedColumnIndex0: Int, tableColumnIndex1: Int, value: Any?) {
            g.buf.appendln(g.metaData.getColumnName(renderedColumnIndex0) + ": " + value.toString())
        }

        override fun afterAllColumnsInRow() {
            g.buf.appendln()
        }
    }})

    fun linePerSeveralColumns(numColumns: Int) = appender({g-> object : Appender {
        override fun appendColumn(renderedColumnIndex0: Int, tableColumnIndex1: Int, value: Any?) {
            if (renderedColumnIndex0 % numColumns == 0 && renderedColumnIndex0 > 0)
                g.buf.appendln()
            g.buf.append(g.metaData.getColumnName(tableColumnIndex1) + ": " + value.toString() + " | ")
        }

        override fun afterAllColumnsInRow() {
            g.buf.append("\n\n")
        }
    }})

    fun linePerRow() = appender({g-> object : Appender {
        override fun appendColumn(renderedColumnIndex0: Int, tableColumnIndex1: Int, value: Any?) {
            if (renderedColumnIndex0 > 0)
                g.buf.append("|")
            g.buf.append(value.toString())
        }

        override fun afterAllColumnsInRow() {
            g.buf.appendln()
        }
    }})

    class Ignition(val opts: Opts) {
        val buf = StringBuilder()
        var metaData by notNullOnce<ResultSetMetaData>()

        fun ignite(): String {
            DBPile.withConnection {con ->
                val st = con.prepareStatement(opts.sql)
                val rs = st.executeQuery()
                metaData = rs.metaData
                val titleValue = when (opts.title) {
                    is Title.None -> null
                    is Title.Explicit -> opts.title.value
                    is Title.AutoDetect -> imf("c5e8ecf9-cda0-43d4-aa7e-67d86e455978")
                    is Title.SQL -> opts.sql
                }
                if (titleValue != null) {
                    buf.appendln(titleValue)
                    buf.appendln("=".repeat(titleValue.length))
                    buf.appendln()
                }
                val appender = opts.appender(this)
                while (rs.next()) {
                    var renderedColumnIndex0 = 0
                    for (columnIndex in 1..metaData.columnCount) {
                        if (!opts.skipColumn(ResultSetColumnMeta(metaData, columnIndex))) {
                            val value: Any? = rs.getObject(columnIndex)
                            appender.appendColumn(renderedColumnIndex0++, columnIndex, value)
                        }
                    }
                    appender.afterAllColumnsInRow()
                }
            }
            return buf.toString()
        }
    }

}

















