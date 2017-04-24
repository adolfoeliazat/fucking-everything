package vgrechka.spewgentests

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
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

object TestGeneratedEntitiesForAmazingWords {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(AppConfig::class.java)

        backPlatform.tx {
            clog("Fucking around?")
            val repo = backPlatform.springctx.getBean(AmazingWordRepository::class.java)!!
            val items = repo.findAll()
            for (item in items) {
                clog(item)
            }
        }

        clog("OK")
    }

    @Suppress("unused")
    @Configuration
    @EnableJpaRepositories
    @EnableTransactionManagement
    @ComponentScan(basePackages = arrayOf("vgrechka.spewgentests"))
    open class AppConfig {
        @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean() -{o->
            o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
                o.setShowSql(true)
            }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
            o.jpaPropertyMap.put(Environment.DIALECT, SQLiteDialect::class.qualifiedName)
            o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
            o.setPackagesToScan("vgrechka.spewgentests")
            o.dataSource = dataSource
        }

        @Bean open fun dataSource(): DataSource {
            return SQLiteConnectionPoolDataSource()-{o->
                o.url = BigPile.localSQLiteShebangDBURL
            }
        }

        @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager() -{o->
            o.entityManagerFactory = emf
        }
    }
}

interface AmazingWordRepository : XCrudRepository<AmazingWord, Long>

@XEntity @XTable(name = "amazing_words")
data class AmazingWord(
    @XEmbedded var amazingWord: AmazingWordFields
) : ClitoralEntity0()

@XEmbeddable
data class AmazingWordFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var word: String,
    @XColumn var rank: Integer
)



