/*
 * (C) Copyright 2017 Vladimir Grechka
 *
 * YOU DON'T MESS AROUND WITH THIS SHIT, IT WAS GENERATED BY A TOOL SMARTER THAN YOU
 */

//
// Generated on Tue May 02 15:59:04 EEST 2017
// Model: e:/fegh/spew-gen-tests/src/GeneratedEntitiesForAmazingWordsTest.kt
//

package vgrechka.spewgentests

import kotlin.reflect.KClass
import vgrechka.*
import vgrechka.spew.*
import vgrechka.db.*

// ------------------------------------------------------------------
// AmazingWord
// ------------------------------------------------------------------

fun newAmazingWord(word: String,
                   rank: Int): AmazingWord {
    val backing = Generated_AmazingWord(
        Generated_AmazingWordFields(word = word,
                                    rank = rank))
    return backing.toManuallyDefinedInterface()
}

val AmazingWord._backing
    get() = (this as Generated_AmazingWordBackingProvider)._backing

val amazingWordRepo: AmazingWordRepository by lazy {
    val generatedRepo = backPlatform.springctx.getBean(Generated_AmazingWordRepository::class.java)!!

    object:AmazingWordRepository {
        override fun findOne(id: Long): AmazingWord? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<AmazingWord> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AmazingWord): AmazingWord {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : AmazingWord) {
            generatedRepo.delete(x._backing)
        }

        override fun findByWordLikeIgnoreCase(x: String): List<AmazingWord> {
            val shit = generatedRepo.findByAmazingWord_WordLikeIgnoreCase(x)
            return shit.map {it.toManuallyDefinedInterface()}
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

// ------------------------------------------------------------------
// AmazingComment
// ------------------------------------------------------------------

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
        override fun findOne(id: Long): AmazingComment? {
            val shit = generatedRepo.findOne(id)
            return shit?.toManuallyDefinedInterface()
        }

        override fun findAll(): List<AmazingComment> {
            val shit = generatedRepo.findAll()
            return shit.map {it.toManuallyDefinedInterface()}
        }

        override fun save(x: AmazingComment): AmazingComment {
            val shit = generatedRepo.save(x._backing)
            return shit.toManuallyDefinedInterface()
        }

        override fun delete(id: Long) {
            generatedRepo.delete(id)
        }

        override fun delete(x : AmazingComment) {
            generatedRepo.delete(x._backing)
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



/*
DDL
===

drop table if exists `amazing_words`;
create table `amazing_words` (
    `id` integer primary key autoincrement,
    `amazingWord_common_createdAt` text not null,
    `amazingWord_common_updatedAt` text not null,
    `amazingWord_common_deleted` integer not null,
    `amazingWord_word` text not null,
    `amazingWord_rank` integer not null
);

drop table if exists `amazing_comments`;
create table `amazing_comments` (
    `id` integer primary key autoincrement,
    `amazingComment_common_createdAt` text not null,
    `amazingComment_common_updatedAt` text not null,
    `amazingComment_common_deleted` integer not null,
    `amazingComment_author` text not null,
    `amazingComment_content` text not null,
    `amazingComment_word__id` bigint not null,
    foreign key (amazingComment_word__id) references amazing_words(id)
);

*/