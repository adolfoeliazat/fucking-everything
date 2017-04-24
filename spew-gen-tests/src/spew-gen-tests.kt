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
import org.springframework.stereotype.Component
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

        exerciseViaManuallyDefinedInterfaces()
//        exerciseDirectlyViaGeneratedCode()

        clog("OK")
    }

    private fun exerciseViaManuallyDefinedInterfaces() {
        backPlatform.tx {tx->
            clog("Fucking around exerciseViaManuallyDefinedInterfaces()?")
            try {
                val repo = amazingWordRepo
//                val repo = backPlatform.springctx.getBean(AmazingWordRepository::class.java)!!

                run {
                    clogSection("All shit")
                    val items = repo.findAll()
                    for (item in items) {
                        clog(item)
                    }
                }

                fun showShitContainingLetterI() {
                    val items = repo.findByWordLikeIgnoreCase("%i%")
                    for (item in items) {
                        clog("#${item.rank.toString().padEnd(5)}${item.word}")
                        // clog(item)
                    }
                }
                run {
                    clogSection("Containing letter 'i'")
                    showShitContainingLetterI()
                }

                run {
                    clogSection("Adding another nice word")
                    repo.save(newAmazingWord(
                        word = "Pizda",
                        rank = 500))
                }

                run {
                    clogSection("Containing letter 'i' again")
                    showShitContainingLetterI()
                }
            } finally {
                tx.setRollbackOnly()
            }
        }
    }

    private fun exerciseDirectlyViaGeneratedCode() {
        backPlatform.tx {tx->
            clog("Fucking around with exerciseDirectlyViaGeneratedCode()?")

            try {
                val repo = backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!

                run {
                    clogSection("All shit")
                    val items = repo.findAll()
                    for (item in items) {
                        clog(item)
                    }
                }

                fun showShitContainingLetterI() {
                    val items = repo.findByAmazingWord_WordLikeIgnoreCase("%i%")
                    for (item in items) {
                        clog("#${item.amazingWord.rank.toString().padEnd(5)}${item.amazingWord.word}")
                        // clog(item)
                    }
                }
                run {
                    clogSection("Containing letter 'i'")
                    showShitContainingLetterI()
                }

                run {
                    clogSection("Adding another nice word")
                    repo.save(Generated_AmazingWord(Generated_AmazingWordFields(
                        word = "Pizda",
                        rank = 500)))
                }

                run {
                    clogSection("Containing letter 'i' again")
                    showShitContainingLetterI()
                }
            } finally {
                tx.setRollbackOnly()
            }
        }
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

        @Bean open fun transactionManager(emf: EntityManagerFactory) = JpaTransactionManager()-{o->
            o.entityManagerFactory = emf
        }
    }
}

//--------------------------- DEFINED MANUALLY ----------------------------

annotation class PersistentShit

interface AmazingWordRepository {
    fun findAll(): List<AmazingWord>
    fun findByWordLikeIgnoreCase(x: String): List<AmazingWord>
    fun save(x: AmazingWord): AmazingWord
}

@PersistentShit interface AmazingWord {
    var word: String
    var rank: Int
}

//--------------------------- SHOULD BE GENERATED ----------------------------

fun newAmazingWord(
    word: String,
    rank: Int
)
    : AmazingWord
{
    val backing = Generated_AmazingWord(Generated_AmazingWordFields(word = word, rank = rank))
    return backing.toManuallyDefinedInterface()
}

val amazingWordRepo: AmazingWordRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!

    object:AmazingWordRepository {
        override fun findAll(): List<AmazingWord> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun findByWordLikeIgnoreCase(x: String): List<AmazingWord> {
            val shit = generatedRepo.findByAmazingWord_WordLikeIgnoreCase(x)
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AmazingWord): AmazingWord {
            val shit = generatedRepo.save((x as Generated_AmazingWordBackingProvider).backing)
            return shit.toManuallyDefinedInterface()
        }
    }
}

//@Component class Generated_AmazingWordRepositoryBean : AmazingWordRepository {
//    private val generatedRepo by lazy {backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!}
//
//    override fun findAll(): List<AmazingWord> {
//        val shit = generatedRepo.findAll()
//        return shit.map {it.toManuallyDefinedInterface()}
//    }
//
//    override fun findByWordLikeIgnoreCase(x: String): List<AmazingWord> {
//        val shit = generatedRepo.findByAmazingWord_WordLikeIgnoreCase(x)
//        return shit.map {it.toManuallyDefinedInterface()}
//    }
//
//    override fun save(x: AmazingWord): AmazingWord {
//        val shit = generatedRepo.save((x as Generated_AmazingWordBackingProvider).backing)
//        return shit.toManuallyDefinedInterface()
//    }
//}

interface Generated_AmazingWordRepository : XCrudRepository<Generated_AmazingWord, Long> {
    fun findByAmazingWord_WordLikeIgnoreCase(x: String): List<Generated_AmazingWord>
}

interface Generated_AmazingWordBackingProvider {
    val backing: Generated_AmazingWord
}

@XEntity @XTable(name = "amazing_words")
data class Generated_AmazingWord(
    @XEmbedded var amazingWord: Generated_AmazingWordFields
)
    : ClitoralEntity0()
{
    fun toManuallyDefinedInterface(): AmazingWord {
        return object : AmazingWord, Generated_AmazingWordBackingProvider {
            override val backing: Generated_AmazingWord
                get() = this@Generated_AmazingWord

            override var word: String
                get() = amazingWord.word
                set(value) {amazingWord.word = value}

            override var rank: Int
                get() = amazingWord.rank
                set(value) {amazingWord.rank = value}
        }
    }
}

@XEmbeddable
data class Generated_AmazingWordFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var word: String,
    @XColumn var rank: Int
)







