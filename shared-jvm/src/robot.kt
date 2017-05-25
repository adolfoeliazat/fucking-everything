package vgrechka

import java.awt.Robot
import java.awt.event.KeyEvent

object RobotPile {
    val robot = Robot()

    fun typeTextCR(text: String) {
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
                    '?' -> {holdShift = true; KeyEvent.VK_SLASH}
                    '=' -> KeyEvent.VK_EQUALS
                    '\\' -> KeyEvent.VK_BACK_SLASH
                    else -> wtf("Dunno how to type key `$c`    2712c548-90bb-42bc-9efb-0b44abec12a2")
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
        key(KeyEvent.VK_ENTER)
    }

    fun key(key: Int) {
        robot.keyPress(key)
        robot.keyRelease(key)
    }

    fun keyWithModifier(modifierKey: Int, key: Char) {
        robot.keyPress(modifierKey)
        robot.keyPress(key.toInt())
        robot.keyRelease(key.toInt())
        robot.keyRelease(modifierKey)
    }

}