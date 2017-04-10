@file:Suppress("Unused")
package vgrechka.idea.hripos

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.WindowManager
import vgrechka.*
import vgrechka.ideabackdoor.*
import javax.swing.JFrame

@Ser class Command_PhiMakeSenseOfPHPSpew(val spew: String) : Servant {
    override fun serve() = serveMumblingCommand("fegh") {con->
        con.mumble(spew)
        con.mumble("OK")
    }
}

object Command_PhiMakeSenseOfPHPSpewTest {
    @JvmStatic
    fun main(args: Array<String>) {
        sendCommandToIDEABackdoor(Command_PhiMakeSenseOfPHPSpew("""
PHP  24. PhiConditional->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1953
PHP  25. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2360
PHP  26. PhiBinaryOperation->testReferenceEquality() /media/sf_phizdetsc-php/phi-engine.php:1481
PHP  27. PhiUnaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1416
PHP  28. PhiBinaryOperation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:2006
PHP  29. PhiInvocation->evaluate() /media/sf_phizdetsc-php/phi-engine.php:1493
PHP  30. phiEval() /media/sf_phizdetsc-php/phi-engine.php:2249
PHP  31. eval() /media/sf_phizdetsc-php/phi-engine.php:2609
PHP Fatal error:  Uncaught Exception: Illegal cast in /media/sf_phizdetsc-php/phi-engine.php:1909
Stack trace:
#0 /media/sf_phizdetsc-php/phizdets-stdlib.php(2123): phiThrow(Object(PhiNew))
#1 /media/sf_phizdetsc-php/phi-engine.php(339): {closure}()
#2 /media/sf_phizdetsc-php/phi-engine.php(2282): PhiFunction->invoke(Object(PhiObject), Array)
#3 /media/sf_phizdetsc-php/phi-engine.php(2364): PhiInvocation->evaluate()
#4 /media/sf_phizdetsc-php/phi-engine.php(1953): PhiConditional->evaluate()
#5 /media/sf_phizdetsc-php/fuck-around--aps-back.php(4284): phiEvaluate(Object(PhiConditional))
#6 /media/sf_phizdetsc-php/phi-engine.php(339): {closure}()
#7 /media/sf_phizdetsc-php/phi-engine.php(168): PhiFunction->invoke(Object(PhiObject), Array)
#8 /media/sf_phizdetsc-php/phi-engine.php(175): PhiObject->getProperty('queryString', Array)
#9 /media/sf_phizdetsc-php/phi-engine.php(54): PhiObject->getProperty('queryString')
#10 /media/sf_phizdetsc-php/phi-engine.php(2280): PhiDot->evaluate()
#11 /media/sf_phizdetsc-php/phi-engine.php(2439): in /media/sf_phizdetsc-php/phi-engine.php on line 1909

Process finished with exit code 255
        """))
    }
}


















