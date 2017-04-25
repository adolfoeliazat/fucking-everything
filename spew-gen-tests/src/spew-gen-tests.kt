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
import vgrechka.spew.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

// TODO:vgrechka Drop/create schema automatically

// TODO:vgrechka Make this into test

object TestGeneratedEntitiesForAmazingWords {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(AppConfig::class.java)

        exerciseViaManuallyDefinedInterfaces()
//        exerciseDirectlyViaGeneratedCode()

        clog("OK")
    }

    private fun exerciseViaManuallyDefinedInterfaces() {
        clog("Fucking around with exerciseViaManuallyDefinedInterfaces()?")
        object:fuckAround<AmazingWord, AmazingComment>() {
            override fun saveCommentToRepo(word: AmazingWord, author: String, content: String): AmazingComment {
                return amazingCommentRepo.save(newAmazingComment(
                    word = word, author = author, content = content))}

            override fun saveWordToRepo(word: String, rank: Int): AmazingWord {
                return amazingWordRepo.save(newAmazingWord(
                    word = word, rank = rank))}

            override fun findAllWords() = amazingWordRepo.findAll()
            override fun wordToString(word: AmazingWord) = word.toString()
            override fun getWordID(word: AmazingWord) = word.id
            override fun getWordWord(word: AmazingWord) = word.word
            override fun getWordRank(word: AmazingWord) = word.rank
            override fun getWordComments(word: AmazingWord) = word.comments
            override fun commentToString(comment: AmazingComment) = comment.toString()
            override fun getCommentContent(comment: AmazingComment) = comment.content
            override fun getCommentAuthor(comment: AmazingComment) = comment.author
            override fun findWordsLikeIgnoreCase(pattern: String) = amazingWordRepo.findByWordLikeIgnoreCase("%i%")
        }
    }

    private fun exerciseDirectlyViaGeneratedCode() {
        clog("Fucking around with exerciseDirectlyViaGeneratedCode()?")
        val wordRepo = backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!
        val commentRepo = backPlatform.springctx.getBean(Generated_AmazingCommentRepository::class.java)!!
        object:fuckAround<Generated_AmazingWord, Generated_AmazingComment>() {
            override fun saveWordToRepo(word: String, rank: Int): Generated_AmazingWord {
                return wordRepo.save(Generated_AmazingWord(Generated_AmazingWordFields(
                    word = word, rank = rank)))}

            override fun saveCommentToRepo(word: Generated_AmazingWord, author: String, content: String): Generated_AmazingComment {
                return commentRepo.save(Generated_AmazingComment(Generated_AmazingCommentFields(
                    word = word, author = author, content = content)))}

            override fun findAllWords() = wordRepo.findAll()
            override fun wordToString(word: Generated_AmazingWord) = word.toString()
            override fun getWordID(word: Generated_AmazingWord) = word.id!!
            override fun getWordWord(word: Generated_AmazingWord) = word.amazingWord.word
            override fun getWordRank(word: Generated_AmazingWord) = word.amazingWord.rank
            override fun getWordComments(word: Generated_AmazingWord) = word.amazingWord.comments
            override fun commentToString(comment: Generated_AmazingComment) = comment.toString()
            override fun getCommentContent(comment: Generated_AmazingComment) = comment.amazingComment.content
            override fun getCommentAuthor(comment: Generated_AmazingComment) = comment.amazingComment.author
            override fun findWordsLikeIgnoreCase(pattern: String) = wordRepo.findByAmazingWord_WordLikeIgnoreCase("%i%")
        }
    }

    abstract class fuckAround<Word, Comment> {
        abstract fun saveWordToRepo(word: String, rank: Int): Word
        abstract fun findAllWords(): Iterable<Word>
        abstract fun wordToString(word: Word): String
        abstract fun getWordID(word: Word): Long
        abstract fun getWordWord(word: Word): String
        abstract fun getWordRank(word: Word): Int
        abstract fun getWordComments(word: Word): MutableList<Comment>
        abstract fun saveCommentToRepo(word: Word, author: String, content: String): Comment
        abstract fun commentToString(comment: Comment): String
        abstract fun findWordsLikeIgnoreCase(pattern: String): List<Word>
        abstract fun getCommentContent(comment: Comment): String
        abstract fun getCommentAuthor(comment: Comment): String

        init {
            backPlatform.tx {tx->
                try {
                    fun dumpComments(pile: List<Comment>, commentToString: (Comment) -> String) {
                        if (pile.isEmpty()) {
                            clog("    (No fucking comments)")
                        } else {
                            for (comment in pile) {
                                clog("    ${commentToString(comment)}")
                            }
                        }
                    }

                    fun dumpWordsAlongWithComments(title: String,
                                                   findWords: () -> Iterable<Word>,
                                                   wordToString: (Word) -> String,
                                                   getComments: (Word) -> List<Comment>,
                                                   commentToString: (Comment) -> String) {
                        clogSection(title)
                        val words = findWords()
                        for (word in words) {
                            clog(wordToString(word))
                            dumpComments(getComments(word), commentToString = commentToString)
                        }
                    }

                    dumpWordsAlongWithComments("All words",
                                               findWords = this::findAllWords,
                                               wordToString = this::wordToString,
                                               getComments = this::getWordComments,
                                               commentToString = this::commentToString)

                    fun showShitContainingLetterI() {
                        clogSection("Words containing letter 'i'")
                        val words = findWordsLikeIgnoreCase("%i%")
                        for (word in words) {
                            clog("#${getWordRank(word).toString().padEnd(5)}${getWordWord(word)}")
                            dumpComments(getWordComments(word), commentToString = {"Comment by ${getCommentAuthor(it)}: ${getCommentContent(it)}"})
                        }
                    }

                    showShitContainingLetterI()

                    run {
                        clogSection("Adding another nice word")
                        val newWord = saveWordToRepo(word = "Pizda", rank = 500)

                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Fucko", content = "Невзъебенно"))
                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Shmacko", content = "Мрак, бля"))
                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Pidoracko", content = "Пеши исчо"))

                        clog("ID of newly added word is ${getWordID(newWord)}")
                    }

                    showShitContainingLetterI()
                } finally {
                    tx.setRollbackOnly()
                }
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
                o.setShowSql(false)
//                o.setShowSql(true)
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

// =========================== DEFINED MANUALLY ============================

annotation class PersistentShit

@PersistentShit interface AmazingWord {
    var id: Long
    var word: String
    var rank: Int
    var comments: MutableList<AmazingComment>
}

interface AmazingWordRepository {
    fun findAll(): List<AmazingWord>
    fun findByWordLikeIgnoreCase(x: String): List<AmazingWord>
    fun save(x: AmazingWord): AmazingWord
}

@PersistentShit interface AmazingComment {
    var author: String
    var content: String
}

interface AmazingCommentRepository {
    fun findAll(): List<AmazingComment>
    fun save(x: AmazingComment): AmazingComment
}


// =========================== SHOULD BE GENERATED ============================


// ---------- AmazingWord ----------


fun newAmazingWord(word: String,
                   rank: Int) : AmazingWord {
    val backing = Generated_AmazingWord(Generated_AmazingWordFields(word = word, rank = rank))
    return backing.toManuallyDefinedInterface()
}

val AmazingWord._backing
    get() = (this as Generated_AmazingWordBackingProvider)._backing

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
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }
    }
}

interface Generated_AmazingWordRepository : XCrudRepository<Generated_AmazingWord, Long> {
    fun findByAmazingWord_WordLikeIgnoreCase(x: String): List<Generated_AmazingWord>
}

interface Generated_AmazingWordBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_AmazingWord> {
    override val _backing: Generated_AmazingWord
}

@XEntity @XTable(name = "amazing_words")
class Generated_AmazingWord(
    @XEmbedded var amazingWord: Generated_AmazingWordFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<AmazingWord>
{
    override fun toManuallyDefinedInterface(): AmazingWord {
        return object : AmazingWord, Generated_AmazingWordBackingProvider {
            override val _backing: Generated_AmazingWord
                get() = this@Generated_AmazingWord

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var word: String
                get() = _backing.amazingWord.word
                set(value) {_backing.amazingWord.word = value}

            override var rank: Int
                get() = _backing.amazingWord.rank
                set(value) {_backing.amazingWord.rank = value}

            override var comments: MutableList<AmazingComment>
                by DBCodeGenUtils.FuckingList(getBackingList = {_backing.amazingWord.comments})

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_AmazingWordBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "AmazingWord(word=${amazingWord.word}, rank=${amazingWord.rank})"
    }
}

@XEmbeddable
class Generated_AmazingWordFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var word: String,
    @XColumn var rank: Int,
    @XOneToMany(fetch = XFetchType.LAZY, mappedBy = "amazingComment.word") var comments: MutableList<Generated_AmazingComment> = mutableListOf()
)


// ---------- AmazingComment ----------


fun newAmazingComment(author: String,
                      content: String,
                      word: AmazingWord) : AmazingComment {
    val backing = Generated_AmazingComment(
        Generated_AmazingCommentFields(author = author,
                                       content = content,
                                       word = word._backing))
    return backing.toManuallyDefinedInterface()
}

val AmazingComment._backing
    get() = (this as Generated_AmazingCommentBackingProvider)._backing

val amazingCommentRepo: AmazingCommentRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_AmazingCommentRepository::class.java)!!

    object:AmazingCommentRepository {
        override fun findAll(): List<AmazingComment> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AmazingComment): AmazingComment {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }
    }
}

interface Generated_AmazingCommentRepository : XCrudRepository<Generated_AmazingComment, Long> {
}

interface Generated_AmazingCommentBackingProvider : DBCodeGenUtils.GeneratedBackingEntityProvider<Generated_AmazingComment> {
    override val _backing: Generated_AmazingComment
}

@XEntity @XTable(name = "amazing_comments")
class Generated_AmazingComment(
    @XEmbedded var amazingComment: Generated_AmazingCommentFields
)
    : ClitoralEntity0(), DBCodeGenUtils.GeneratedEntity<AmazingComment>
{
    override fun toManuallyDefinedInterface(): AmazingComment {
        return object : AmazingComment, Generated_AmazingCommentBackingProvider {
            override val _backing: Generated_AmazingComment
                get() = this@Generated_AmazingComment

            override var author: String
                get() = _backing.amazingComment.author
                set(value) {_backing.amazingComment.author = value}

            override var content: String
                get() = _backing.amazingComment.content
                set(value) {_backing.amazingComment.content = value}

            override fun toString() = _backing.toString()

            override fun hashCode() = _backing.hashCode()

            override fun equals(other: Any?): Boolean {
                val otherShit = other as? Generated_AmazingCommentBackingProvider ?: return false
                return _backing == otherShit._backing
            }
        }
    }

    override fun toString(): String {
        return "AmazingComment(author=${amazingComment.author}, content=${amazingComment.content})"
    }
}

@XEmbeddable
class Generated_AmazingCommentFields(
    @XEmbedded var common: CommonFields = CommonFields(),
    @XColumn(columnDefinition = "text") var author: String,
    @XColumn(columnDefinition = "text") var content: String,
    @XManyToOne(fetch = XFetchType.LAZY) var word: Generated_AmazingWord
)












