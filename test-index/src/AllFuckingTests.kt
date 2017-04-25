package vgrechka.testindex

import org.junit.runner.RunWith
import org.junit.runners.Suite
import vgrechka.*
import vgrechka.spewgentests.*

@RunWith(Suite::class) @Suite.SuiteClasses(
    SpewGenTestSuite::class,
    SharedJVMTestSuite::class
)
class AllFuckingTests



