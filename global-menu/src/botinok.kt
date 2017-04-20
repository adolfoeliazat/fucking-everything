package vgrechka.botinok

import javafx.scene.control.Label
import javafx.scene.control.TextArea
import org.jnativehook.keyboard.NativeKeyEvent
import vgrechka.globalmenu.*
import java.awt.MouseInfo
import kotlin.properties.Delegates.notNull

internal class BotinokGlobalMenuConfig : GlobalMenuConfig {
    override val faces = listOf(
        makeListFace(
            keyCode = NativeKeyEvent.VC_1,
            items = listOf(
                object:MenuItem() {
                    private var textArea by notNull<TextArea>()

                    override fun toString() = "Get mouse location"

                    override fun run() {
                        showDamnLocation()
                    }

                    override fun makeDetailsControl(): TextArea {
                        textArea = TextArea()
                        textArea.minHeight = 100.0
                        showDamnLocation()
                        return textArea
                    }

                    private fun showDamnLocation() {
                        val location = MouseInfo.getPointerInfo().location
                        textArea.text = "${location.x}, ${location.y}"
                    }
                }
            )
        ),

        BotinokScreenshotFace(NativeKeyEvent.VC_2)

//        makeFuckingFace(
//            keyCode = NativeKeyEvent.VC_3,
//            items = listOf(
//                fuckingSimpleMenuItem(this::playScenario1)
//            )
//        )
    )

    fun playScenario1() {
        JFXStuff.errorAlert("Not now, hoser")
    }

}

internal class BotinokScreenshotFace(override val keyCode: Int) : GlobalMenuFace() {
    override val rootControl = Label("fucking shit")

    override fun onDeiconified() {
        rootControl.text = "pizdarius"
        GlobalMenuPile.resizePrimaryStage(1000, 500)
    }
}











