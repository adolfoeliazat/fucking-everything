package vgrechka

import java.awt.Robot
import java.awt.event.KeyEvent

object RobotPile {
    val robot = Robot()

    fun typeTextCR(text: String) {
        val robot = Robot()
        text.forEach {c->
            // dwarnStriking("Typing key: " + c)
            var holdShift = c.isLetter() && c.isUpperCase()

            val keyCode = when {
                c.isLetterOrDigit() -> c.toUpperCase().toInt()
                else -> when (c) {
                    ' ' -> KeyEvent.VK_SPACE
                    ':' -> {holdShift = true; KeyEvent.VK_SEMICOLON}
                    '&' -> {holdShift = true; KeyEvent.VK_7}
                    '.' -> KeyEvent.VK_PERIOD
                    '/' -> KeyEvent.VK_SLASH
                    '\\' -> KeyEvent.VK_BACK_SLASH
                    else -> wtf("Dunno how to type key `$c`")
                }
            }

            if (holdShift)
                robot.keyPress(KeyEvent.VK_SHIFT)

            try {
                robot.keyPress(keyCode)
                robot.keyRelease(keyCode)
            } finally {
                if (holdShift)
                    robot.keyRelease(KeyEvent.VK_SHIFT)
            }
        }
        robot.keyPress(KeyEvent.VK_ENTER)
        robot.keyRelease(KeyEvent.VK_ENTER)
    }
}