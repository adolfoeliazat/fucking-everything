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
import vgrechka.*
import vgrechka.db.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource


@Suppress("unused")
@EnableJpaRepositories
@ComponentScan(basePackages = arrayOf("vgrechka.botinok"))
abstract class BotinokBaseAppConfig : BaseSQLiteAppConfig(entityPackagesToScan = arrayOf("vgrechka.botinok")) {
}

open class BotinokProdAppConfig : BotinokBaseAppConfig() {
    override val databaseURL = BigPile.localSQLiteShebangDBURL
}

open class BotinokTestAppConfig : BotinokBaseAppConfig() {
    override val databaseURL = BigPile.localSQLiteShebangTestDBURL
}

















