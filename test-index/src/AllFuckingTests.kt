package vgrechka.testindex

import org.junit.runner.RunWith
import org.junit.runners.Suite
import vgrechka.*
import vgrechka.botinok.*
import vgrechka.spewgentests.*
import java.io.File

@RunWith(Suite::class) @Suite.SuiteClasses(
    SpewGenTestSuite::class,
    SharedJVMTestSuite::class,
    BotinokTests::class
)
class AllFuckingTests


