package vgrechka.db

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
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.sqlite.SQLiteConfig
import org.sqlite.javax.IntoSQLiteConnectionPoolDataSource
import vgrechka.*
import java.sql.Connection
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

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

    private fun <T> withConnection(block: (Connection) -> T): T {
        val ds = backPlatform.springctx.getBean(DataSource::class.java)
        val con = ds.connection
        try {
            return block(con)
        } finally {
            con.close()
        }
    }

    fun executeAndFormatResultForPrinting(sql: String): String {
        return withConnection {con->
            val buf = StringBuilder()
            val st = con.prepareStatement(sql)
            val rs = st.executeQuery()
            while (rs.next()) {
                for (columnIndex in 1..rs.metaData.columnCount) {
                    val value: Any? = rs.getObject(columnIndex)
                    if (columnIndex > 1)
                        buf.append("|")
                    buf.append(value.toString())
                }
                buf.appendln()
            }
            buf.toString()
        }
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
abstract class BaseSQLiteAppConfig(val entityPackagesToScan: Array<String>) {
    protected abstract val databaseURL: String

    @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean()-{o->
        o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
            o.setShowSql(true)
        }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
        o.jpaPropertyMap.put(Environment.DIALECT, SQLiteDialect::class.qualifiedName)
        o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
        o.setPackagesToScan(*entityPackagesToScan)
        o.dataSource = dataSource
    }

    @Bean open fun dataSource(): DataSource {
//        return SQLiteConnectionPoolDataSource()-{o->
        return IntoSQLiteConnectionPoolDataSource()-{o->
            o.url = databaseURL
            o.config = SQLiteConfig()-{o->
                o.setDateClass(SQLiteConfig.DateClass.TEXT.value)
                o.enforceForeignKeys(true)
            }
        }
    }

    @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
        o.entityManagerFactory = emf
    }

}

abstract class BaseTestSQLiteAppConfig(entityPackagesToScan: Array<String>): BaseSQLiteAppConfig(entityPackagesToScan) {
    override val databaseURL = BigPile.localSQLiteShebangTestDBURL
}


















