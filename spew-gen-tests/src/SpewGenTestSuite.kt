package vgrechka.spewgentests

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import vgrechka.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    GeneratedEntitiesForAmazingWordsTest::class
)
class SpewGenTestSuite

object Spike_SpringDataJPA {
    @JvmStatic
    fun main(args: Array<String>) {
        backPlatform.springctx = AnnotationConfigApplicationContext(GeneratedEntitiesForAmazingWordsTest.AppConfig::class.java)
        val word = backPlatform.tx {
            amazingWordRepo.findAll().first()
        }
        clog(word.word)

        word.comments.add(newAmazingComment("fuck", "shit", word))
        word.word = "aaaaaaaaaa2"
        amazingWordRepo.save(word)

        clog("...and yeah, fuck you")
    }
}

