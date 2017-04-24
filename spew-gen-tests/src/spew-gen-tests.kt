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

// TODO:vgrechka Test insertion of many-to-one entity

// TODO:vgrechka Go through following implementations and make sure semantics of backing objects is preserved:
//     c3c230b5-ab1d-44bb-acbe-e9db123583b1
//     aae83eee-f0c9-42fb-9cd2-ab25b87cd4cd
//     e42ac601-2330-4e6e-8bcb-9c631870b3dd

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
            override fun saveNewWordToRepo(word: String, rank: Int): AmazingWord {
                return amazingWordRepo.save(newAmazingWord(
                    word = "Pizda",
                    rank = 500))}
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
        val repo = backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!
        object:fuckAround<Generated_AmazingWord, Generated_AmazingComment>() {
            override fun saveNewWordToRepo(word: String, rank: Int): Generated_AmazingWord {
                return repo.save(Generated_AmazingWord(Generated_AmazingWordFields(
                    word = "Pizda",
                    rank = 500)))}
            override fun findAllWords() = repo.findAll()
            override fun wordToString(word: Generated_AmazingWord) = word.toString()
            override fun getWordID(word: Generated_AmazingWord) = word.id!!
            override fun getWordWord(word: Generated_AmazingWord) = word.amazingWord.word
            override fun getWordRank(word: Generated_AmazingWord) = word.amazingWord.rank
            override fun getWordComments(word: Generated_AmazingWord) = word.amazingWord.comments
            override fun commentToString(comment: Generated_AmazingComment) = comment.toString()
            override fun getCommentContent(comment: Generated_AmazingComment) = comment.amazingComment.content
            override fun getCommentAuthor(comment: Generated_AmazingComment) = comment.amazingComment.author
            override fun findWordsLikeIgnoreCase(pattern: String) = repo.findByAmazingWord_WordLikeIgnoreCase("%i%")
        }
    }

    abstract class fuckAround<Word, Comment> {
        abstract fun saveNewWordToRepo(word: String, rank: Int): Word
        abstract fun findAllWords(): Iterable<Word>
        abstract fun wordToString(word: Word): String
        abstract fun getWordID(word: Word): Long
        abstract fun getWordWord(word: Word): String
        abstract fun getWordRank(word: Word): Int
        abstract fun getWordComments(word: Word): List<Comment>
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
                        val addedWord = saveNewWordToRepo(word = "Pizda", rank = 500)
                        clog("ID of newly added word is ${getWordID(addedWord)}")
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

fun AmazingWord.toGeneratedBackingEntity() = (this as Generated_AmazingWordBackingProvider)._backing

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
            val shit = generatedRepo.save(x.toGeneratedBackingEntity())
            return shit.toManuallyDefinedInterface()
        }
    }
}

interface Generated_AmazingWordRepository : XCrudRepository<Generated_AmazingWord, Long> {
    fun findByAmazingWord_WordLikeIgnoreCase(x: String): List<Generated_AmazingWord>
}

interface Generated_AmazingWordBackingProvider {
    val _backing: Generated_AmazingWord
}

@XEntity @XTable(name = "amazing_words")
class Generated_AmazingWord(
    @XEmbedded var amazingWord: Generated_AmazingWordFields
)
    : ClitoralEntity0()
{
    fun toManuallyDefinedInterface(): AmazingWord {
        return object : AmazingWord, Generated_AmazingWordBackingProvider {
            override val _backing: Generated_AmazingWord
                get() = this@Generated_AmazingWord

            override var id: Long
                get() = _backing.id!!
                set(value) {_backing.id = value}

            override var word: String
                get() = _backing.amazingWord.word
                set(value) {
                    _backing.amazingWord.word = value}

            override var rank: Int
                get() = _backing.amazingWord.rank
                set(value) {
                    _backing.amazingWord.rank = value}

            override var comments: MutableList<AmazingComment>
                get() {
                    val backingList = _backing.amazingWord.comments
                    return object:MutableList<AmazingComment> { // c3c230b5-ab1d-44bb-acbe-e9db123583b1
                        override val size: Int
                            get() = backingList.size

                        override fun contains(element: AmazingComment): Boolean {
                            return backingList.contains(element.toGeneratedBackingEntity())
                        }

                        override fun containsAll(elements: Collection<AmazingComment>): Boolean {
                            return backingList.containsAll(elements.map {it.toGeneratedBackingEntity()})
                        }

                        override fun get(index: Int): AmazingComment {
                            return backingList[index].toManuallyDefinedInterface()
                        }

                        override fun indexOf(element: AmazingComment): Int {
                            return backingList.indexOf(element.toGeneratedBackingEntity())
                        }

                        override fun isEmpty(): Boolean {
                            return backingList.isEmpty()
                        }

                        override fun iterator(): MutableIterator<AmazingComment> {
                            return backingIteratorToIterator(backingList.iterator())
                        }

                        private fun backingIteratorToIterator(backingIterator: MutableIterator<Generated_AmazingComment>): MutableIterator<AmazingComment> {
                            return object : MutableIterator<AmazingComment> { // e42ac601-2330-4e6e-8bcb-9c631870b3dd
                                override fun remove() {
                                    backingIterator.remove()
                                }

                                override fun hasNext(): Boolean {
                                    return backingIterator.hasNext()
                                }

                                override fun next(): AmazingComment {
                                    return backingIterator.next().toManuallyDefinedInterface()
                                }
                            }
                        }

                        override fun lastIndexOf(element: AmazingComment): Int {
                            return backingList.lastIndexOf(element.toGeneratedBackingEntity())
                        }

                        override fun add(element: AmazingComment): Boolean {
                            return backingList.add(element.toGeneratedBackingEntity())
                        }

                        override fun add(index: Int, element: AmazingComment) {
                            return backingList.add(index, element.toGeneratedBackingEntity())
                        }

                        override fun addAll(index: Int, elements: Collection<AmazingComment>): Boolean {
                            return backingList.addAll(index, elements.map {it.toGeneratedBackingEntity()})
                        }

                        override fun addAll(elements: Collection<AmazingComment>): Boolean {
                            return backingList.addAll(elements.map {it.toGeneratedBackingEntity()})
                        }

                        override fun clear() {
                            return backingList.clear()
                        }

                        override fun listIterator(): MutableListIterator<AmazingComment> {
                            return backingListIteratorToListIterator(backingList.listIterator())
                        }

                        private fun backingListIteratorToListIterator(backingListIterator: MutableListIterator<Generated_AmazingComment>): MutableListIterator<AmazingComment> {
                            return object : MutableListIterator<AmazingComment> { // aae83eee-f0c9-42fb-9cd2-ab25b87cd4cd
                                override fun add(element: AmazingComment) {
                                    return backingListIterator.add(element.toGeneratedBackingEntity())
                                }

                                override fun hasNext(): Boolean {
                                    return backingListIterator.hasNext()
                                }

                                override fun next(): AmazingComment {
                                    return backingListIterator.next().toManuallyDefinedInterface()
                                }

                                override fun remove() {
                                    return backingListIterator.remove()
                                }

                                override fun set(element: AmazingComment) {
                                    return backingListIterator.set(element.toGeneratedBackingEntity())
                                }

                                override fun hasPrevious(): Boolean {
                                    return backingListIterator.hasPrevious()
                                }

                                override fun nextIndex(): Int {
                                    return backingListIterator.nextIndex()
                                }

                                override fun previous(): AmazingComment {
                                    return backingListIterator.previous().toManuallyDefinedInterface()
                                }

                                override fun previousIndex(): Int {
                                    return backingListIterator.previousIndex()
                                }
                            }
                        }

                        override fun listIterator(index: Int): MutableListIterator<AmazingComment> {
                            return backingListIteratorToListIterator(backingList.listIterator(index))
                        }

                        override fun remove(element: AmazingComment): Boolean {
                            return backingList.remove(element.toGeneratedBackingEntity())
                        }

                        override fun removeAll(elements: Collection<AmazingComment>): Boolean {
                            return backingList.removeAll(elements.map {it.toGeneratedBackingEntity()})
                        }

                        override fun removeAt(index: Int): AmazingComment {
                            return backingList.removeAt(index).toManuallyDefinedInterface()
                        }

                        override fun retainAll(elements: Collection<AmazingComment>): Boolean {
                            return backingList.retainAll(elements.map {it.toGeneratedBackingEntity()})
                        }

                        override fun set(index: Int, element: AmazingComment): AmazingComment {
                            return backingList.set(index, element.toGeneratedBackingEntity()).toManuallyDefinedInterface()
                        }

                        override fun subList(fromIndex: Int, toIndex: Int): MutableList<AmazingComment> {
                            imf("a7db4be2-40bc-4cc2-9541-0a8ba80eac43")
                        }

                    }
                }
                set(value) {
                    imf("ca6c7f7a-8da5-4969-bbe8-eea87005a5e7")
                }

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
                                       word = word.toGeneratedBackingEntity()))
    return backing.toManuallyDefinedInterface()
}

fun AmazingComment.toGeneratedBackingEntity() = (this as Generated_AmazingCommentBackingProvider)._backing

val amazingCommentRepo: AmazingCommentRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_AmazingCommentRepository::class.java)!!

    object:AmazingCommentRepository {
        override fun findAll(): List<AmazingComment> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AmazingComment): AmazingComment {
            val shit = generatedRepo.save(x.toGeneratedBackingEntity())
            return shit.toManuallyDefinedInterface()
        }
    }
}

interface Generated_AmazingCommentRepository : XCrudRepository<Generated_AmazingComment, Long> {
    // TODO:vgrechka ...
}

interface Generated_AmazingCommentBackingProvider {
    val _backing: Generated_AmazingComment
}

@XEntity @XTable(name = "amazing_comments")
class Generated_AmazingComment(
    @XEmbedded var amazingComment: Generated_AmazingCommentFields
)
    : ClitoralEntity0()
{
    fun toManuallyDefinedInterface(): AmazingComment {
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












