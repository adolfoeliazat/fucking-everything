package vgrechka.botinok

import org.hibernate.cfg.Environment
import org.hibernate.dialect.SQLiteDialect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.sqlite.SQLiteConfig
import org.sqlite.javax.IntoSQLiteConnectionPoolDataSource
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import vgrechka.*
import vgrechka.db.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource


@Suppress("unused")
@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
@ComponentScan(basePackages = arrayOf("vgrechka.botinok"))
open class BotinokAppConfig {

    @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean()-{o->
        o.jpaVendorAdapter = HibernateJpaVendorAdapter()-{o->
            o.setShowSql(true)
        }
//        o.jpaPropertyMap.put(Environment.HBM2DDL_AUTO, "create-drop")
        o.jpaPropertyMap.put(Environment.DIALECT, SQLiteDialect::class.qualifiedName)
        o.jpaPropertyMap.put(Environment.IMPLICIT_NAMING_STRATEGY, NiceHibernateNamingStrategy::class.qualifiedName)
        o.setPackagesToScan("vgrechka.botinok")
        o.dataSource = dataSource
    }

    @Bean open fun dataSource(): DataSource {
//        return SQLiteConnectionPoolDataSource()-{o->
        return IntoSQLiteConnectionPoolDataSource()-{o->
            o.url = BigPile.localSQLiteShebangDBURL
            o.config = SQLiteConfig()-{o->
                o.setDateClass(SQLiteConfig.DateClass.TEXT.value)
            }
        }
    }

    @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
        o.entityManagerFactory = emf
    }
}


