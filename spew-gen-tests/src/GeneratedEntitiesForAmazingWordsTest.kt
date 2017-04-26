package vgrechka.spewgentests

import org.hibernate.cfg.Environment
import org.hibernate.dialect.SQLiteDialect
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
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

class GeneratedEntitiesForAmazingWordsTest {
    val log = TestLogger()

    companion object {
        @BeforeClass @JvmStatic fun beforeClass() {
            backPlatform.springctx = AnnotationConfigApplicationContext(AppConfig::class.java)
        }

        @AfterClass @JvmStatic fun afterClass() {
            notNullOnce.debugReset(JVMBackPlatform::springctx)
        }
    }

    @Test fun exerciseViaManuallyDefinedInterfaces() {
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

    @Test fun exerciseDirectlyViaGeneratedCode() {
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

    abstract inner class fuckAround<Word, Comment> {
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
            recreateSchema()
            backPlatform.tx {tx->
                try {
                    fun dumpComments(pile: List<Comment>, commentToString: (Comment) -> String) {
                        if (pile.isEmpty()) {
                            log.println("    (No fucking comments)", "07c3b63d-8c1c-47ad-825e-c5efe8b16fe3")
                        } else {
                            for ((i, comment) in pile.withIndex()) {
                                log.println("    ${commentToString(comment)}", "$i--93c0132a-e38d-4413-bde4-c63e7b81a465")
                            }
                        }
                    }

                    fun dumpWordsAlongWithComments(title: String,
                                                   findWords: () -> Iterable<Word>,
                                                   wordToString: (Word) -> String,
                                                   getComments: (Word) -> List<Comment>,
                                                   commentToString: (Comment) -> String) {
                        log.section(title, "d9e74c60-33bc-4758-b3fe-c456cfaeed6a")
                        val words = findWords()
                        for (word in words) {
                            log.println(wordToString(word), "6efc12fd-1c41-43a4-b13d-d77b1c4a23e7")
                            dumpComments(getComments(word), commentToString = commentToString)
                        }
                    }

                    dumpWordsAlongWithComments("All words",
                                               findWords = this::findAllWords,
                                               wordToString = this::wordToString,
                                               getComments = this::getWordComments,
                                               commentToString = this::commentToString)

                    fun showShitContainingLetterI() {
                        log.section("Words containing letter 'i'", "321c69fc-2a56-4b2e-8cc7-7817abb1203c")
                        val words = findWordsLikeIgnoreCase("%i%")
                        for ((i, word) in words.withIndex()) {
                            log.println("#${getWordRank(word).toString().padEnd(5)}${getWordWord(word)}", "$i--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816")
                            dumpComments(getWordComments(word), commentToString = {"Comment by ${getCommentAuthor(it)}: ${getCommentContent(it)}"})
                        }
                    }

                    showShitContainingLetterI()

                    run {
                        log.section("Adding another nice word", "072eecc9-8390-4efc-8fee-7286a7751020")
                        val newWord = saveWordToRepo(word = "Pizda", rank = 500)

                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Fucko", content = "Невзъебенно"))
                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Shmacko", content = "Мрак, бля"))
                        getWordComments(newWord).add(saveCommentToRepo(newWord, author = "Pidoracko", content = "Пеши исчо"))

                        log.println("ID of newly added word is ${getWordID(newWord)}", "3b11683c-7979-4e65-af4e-1afed120503e")
                    }

                    showShitContainingLetterI()
                } finally {
//                    tx.setRollbackOnly()
                }
            }

            log.assertEquals("""
Recreating schema                                          11e89c05-3db6-4b67-a111-2509625d7028

------------- All words -------------                      d9e74c60-33bc-4758-b3fe-c456cfaeed6a

AmazingWord(word=Fuck, rank=15)                            6efc12fd-1c41-43a4-b13d-d77b1c4a23e7
    AmazingComment(author=Ben, content=Wow)                0--93c0132a-e38d-4413-bde4-c63e7b81a465
    AmazingComment(author=Peter, content=Very eloquent)    1--93c0132a-e38d-4413-bde4-c63e7b81a465
AmazingWord(word=Shit, rank=10)                            6efc12fd-1c41-43a4-b13d-d77b1c4a23e7
    AmazingComment(author=Nick, content=Much swearing)     0--93c0132a-e38d-4413-bde4-c63e7b81a465
    AmazingComment(author=Ronald, content=Such polite)     1--93c0132a-e38d-4413-bde4-c63e7b81a465
AmazingWord(word=Bitch, rank=20)                           6efc12fd-1c41-43a4-b13d-d77b1c4a23e7
    (No fucking comments)                                  07c3b63d-8c1c-47ad-825e-c5efe8b16fe3

------------- Words containing letter 'i' -------------    321c69fc-2a56-4b2e-8cc7-7817abb1203c

#10   Shit                                                 0--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816
    Comment by Nick: Much swearing                         0--93c0132a-e38d-4413-bde4-c63e7b81a465
    Comment by Ronald: Such polite                         1--93c0132a-e38d-4413-bde4-c63e7b81a465
#20   Bitch                                                1--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816
    (No fucking comments)                                  07c3b63d-8c1c-47ad-825e-c5efe8b16fe3

------------- Adding another nice word -------------       072eecc9-8390-4efc-8fee-7286a7751020

ID of newly added word is 4                                3b11683c-7979-4e65-af4e-1afed120503e

------------- Words containing letter 'i' -------------    321c69fc-2a56-4b2e-8cc7-7817abb1203c

#10   Shit                                                 0--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816
    Comment by Nick: Much swearing                         0--93c0132a-e38d-4413-bde4-c63e7b81a465
    Comment by Ronald: Such polite                         1--93c0132a-e38d-4413-bde4-c63e7b81a465
#20   Bitch                                                1--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816
    (No fucking comments)                                  07c3b63d-8c1c-47ad-825e-c5efe8b16fe3
#500  Pizda                                                2--9ad63ec7-b4ad-4342-a1bb-91c3b7e2a816
    Comment by Fucko: Невзъебенно                          0--93c0132a-e38d-4413-bde4-c63e7b81a465
    Comment by Shmacko: Мрак, бля                          1--93c0132a-e38d-4413-bde4-c63e7b81a465
    Comment by Pidoracko: Пеши исчо                        2--93c0132a-e38d-4413-bde4-c63e7b81a465
            """)
        }

        private fun recreateSchema() {
            log.println("Recreating schema", "11e89c05-3db6-4b67-a111-2509625d7028")
            val ds = backPlatform.springctx.getBean(DataSource::class.java)
            val con = ds.connection
            try {
                val st = con.createStatement()
                st.execute("drop table if exists amazing_comments;")
                st.execute("drop table if exists amazing_words;")

                st.execute("""
                    create table `amazing_words` (
                        `id` integer primary key autoincrement,
                        `amazingWord_word` text not null,
                        `amazingWord_rank` integer not null,
                        `amazingWord_common_createdAt` text not null,
                        `amazingWord_common_updatedAt` text not null,
                        `amazingWord_common_deleted` integer not null
                    );
                """)
                st.execute("INSERT INTO amazing_words VALUES(1, 'Fuck', 15, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
                st.execute("INSERT INTO amazing_words VALUES(2, 'Shit', 10, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
                st.execute("INSERT INTO amazing_words VALUES(3, 'Bitch', 20, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")

                st.execute("""
                    create table `amazing_comments` (
                        `id` integer primary key autoincrement,
                        `amazingComment_author` text not null,
                        `amazingComment_content` text not null,
                        `amazingComment_word__id` bigint not null,
                        `amazingComment_common_createdAt` text not null,
                        `amazingComment_common_updatedAt` text not null,
                        `amazingComment_common_deleted` integer not null,

                        foreign key (amazingComment_word__id) references amazing_words(id)
                    );
                """)
                st.execute("INSERT INTO amazing_comments VALUES(1, 'Ben', 'Wow', 1, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
                st.execute("INSERT INTO amazing_comments VALUES(2, 'Peter', 'Very eloquent', 1, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
                st.execute("INSERT INTO amazing_comments VALUES(3, 'Nick', 'Much swearing', 2, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
                st.execute("INSERT INTO amazing_comments VALUES(4, 'Ronald', 'Such polite', 2, '2017-01-02 15:23:51.555', '2017-01-02 15:23:51.555', 0);")
            } finally {
                con.close()
            }
        }
    }

    @Suppress("unused")
    @Configuration
    @EnableJpaRepositories
    @EnableTransactionManagement
    @ComponentScan(basePackages = arrayOf("vgrechka.spewgentests"))
    open class AppConfig {
        @Bean open fun entityManagerFactory(dataSource: DataSource) = LocalContainerEntityManagerFactoryBean()-{o->
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

// =========================== LIBRARY ============================
// TODO:vgrechka Move out of tests

annotation class PersistentShit

interface CommonPersistentShitFields {
    var id: Long
    var createdAt: XTimestamp
    var updatedAt: XTimestamp
    var deleted: Boolean
}

// =========================== DEFINED MANUALLY ============================


@PersistentShit interface AmazingWord : CommonPersistentShitFields {
    var word: String
    var rank: Int
    var comments: MutableList<AmazingComment>
}

interface AmazingWordRepository {
    fun findAll(): List<AmazingWord>
    fun findByWordLikeIgnoreCase(x: String): List<AmazingWord>
    fun save(x: AmazingWord): AmazingWord
}

@PersistentShit interface AmazingComment : CommonPersistentShitFields {
    var author: String
    var content: String
    var word: AmazingWord
}

interface AmazingCommentRepository {
    fun findAll(): List<AmazingComment>
    fun save(x: AmazingComment): AmazingComment
}


// =========================== SHOULD BE GENERATED ============================

/*

// ---------- AmazingWord ----------


fun newAmazingWord(word: String,
                   rank: Int): AmazingWord {
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

            override var createdAt: XTimestamp
                get() = _backing.amazingWord.common.createdAt
                set(value) {_backing.amazingWord.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.amazingWord.common.updatedAt
                set(value) {_backing.amazingWord.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.amazingWord.common.deleted
                set(value) {_backing.amazingWord.common.deleted = value}

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
                      word: AmazingWord): AmazingComment {
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

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var createdAt: XTimestamp
                get() = _backing.amazingComment.common.createdAt
                set(value) {_backing.amazingComment.common.createdAt = value}

            override var updatedAt: XTimestamp
                get() = _backing.amazingComment.common.updatedAt
                set(value) {_backing.amazingComment.common.updatedAt = value}

            override var deleted: Boolean
                get() = _backing.amazingComment.common.deleted
                set(value) {_backing.amazingComment.common.deleted = value}

            override var author: String
                get() = _backing.amazingComment.author
                set(value) {_backing.amazingComment.author = value}

            override var content: String
                get() = _backing.amazingComment.content
                set(value) {_backing.amazingComment.content = value}

            override var word: AmazingWord
                get() = _backing.amazingComment.word.toManuallyDefinedInterface()
                set(value) {_backing.amazingComment.word = value._backing}

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

*/












