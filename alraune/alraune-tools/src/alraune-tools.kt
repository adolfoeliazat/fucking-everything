package alraune

import vgrechka.spew.*

object CLI_AlGenerateStyleClass {
    @JvmStatic
    fun main(args: Array<String>) {
        val shit = """
accelerator azimuth background background-attachment background-color background-image background-position background-position-x
background-position-y background-repeat behavior border border-bottom border-bottom-color border-bottom-style border-bottom-width
border-collapse border-color border-left border-left-color border-left-style border-left-width border-right border-right-color
border-right-style border-right-width border-spacing border-style border-top border-top-color border-top-style border-top-width
border-width bottom caption-side clear clip color content counter-increment counter-reset cue cue-after cue-before cursor
direction display elevation empty-cells filter float font font-family font-size font-size-adjust font-stretch font-style
font-variant font-weight height ime-mode include-source layer-background-color layer-background-image layout-flow layout-grid
layout-grid-char layout-grid-char-spacing layout-grid-line layout-grid-mode layout-grid-type left letter-spacing line-break
line-height list-style list-style-image list-style-position list-style-type margin margin-bottom margin-left margin-right
margin-top marker-offset marks max-height max-width min-height min-width orphans outline outline-color outline-style outline-width
overflow overflow-x overflow-y padding padding-bottom padding-left padding-right padding-top page page-break-after page-break-before
page-break-inside pause pause-after pause-before pitch pitch-range play-during position quotes richness right ruby-align
ruby-overhang ruby-position size speak speak-header speak-numeral speak-punctuation speech-rate stress scrollbar-arrow-color
scrollbar-base-color scrollbar-dark-shadow-color scrollbar-face-color scrollbar-highlight-color scrollbar-shadow-color
scrollbar-3d-light-color scrollbar-track-color table-layout text-align text-align-last text-decoration text-indent text-justify
text-overflow text-shadow text-transform text-autospace text-kashida-space text-underline-position top unicode-bidi vertical-align
visibility voice-family volume white-space widows width word-break word-spacing word-wrap writing-mode z-index zoom
        """

        fun kotlinProp(cssProp: String) = buildString {
            for ((i, c) in cssProp.withIndex()) {
                if (c != '-') {
                    append(when {
                        i > 0 && cssProp[i - 1] == '-' -> c.toUpperCase()
                        else -> c
                    })
                }
            }
        }

        val shitter = CodeShitter().apply {
            ln("            class Style(")
            val props = shit.trim().split(Regex("\\s+"))
            for (cssProp in props) {
                ln("                val ${kotlinProp(cssProp)}: String? = null,")
            }
            deleteLastCommaBeforeNewLine()
            ln("            ) {")
            ln("                fun render(): String {")
            ln("                    return buildString {")
            for (cssProp in props) {
                ln("                        ${kotlinProp(cssProp)}?.let {append(\"$cssProp: \$it;\")}")
            }
            ln("                    }")
            ln("                }")
            ln("            }")
        }
        println(shitter.reify())
    }
}




