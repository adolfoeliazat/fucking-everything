package alraune.back

class Style(
    val accelerator: String? = null,
    val azimuth: String? = null,
    val background: String? = null,
    val backgroundAttachment: String? = null,
    val backgroundColor: String? = null,
    val backgroundImage: String? = null,
    val backgroundPosition: String? = null,
    val backgroundPositionX: String? = null,
    val backgroundPositionY: String? = null,
    val backgroundRepeat: String? = null,
    val behavior: String? = null,
    val border: String? = null,
    val borderBottom: String? = null,
    val borderBottomColor: String? = null,
    val borderBottomStyle: String? = null,
    val borderBottomWidth: String? = null,
    val borderCollapse: String? = null,
    val borderColor: String? = null,
    val borderLeft: String? = null,
    val borderLeftColor: String? = null,
    val borderLeftStyle: String? = null,
    val borderLeftWidth: String? = null,
    val borderRight: String? = null,
    val borderRightColor: String? = null,
    val borderRightStyle: String? = null,
    val borderRightWidth: String? = null,
    val borderSpacing: String? = null,
    val borderStyle: String? = null,
    val borderTop: String? = null,
    val borderTopColor: String? = null,
    val borderTopStyle: String? = null,
    val borderTopWidth: String? = null,
    val borderWidth: String? = null,
    val bottom: String? = null,
    val captionSide: String? = null,
    val clear: String? = null,
    val clip: String? = null,
    val color: String? = null,
    val content: String? = null,
    val counterIncrement: String? = null,
    val counterReset: String? = null,
    val cue: String? = null,
    val cueAfter: String? = null,
    val cueBefore: String? = null,
    val cursor: String? = null,
    val direction: String? = null,
    val display: String? = null,
    val elevation: String? = null,
    val emptyCells: String? = null,
    val filter: String? = null,
    val float: String? = null,
    val font: String? = null,
    val fontFamily: String? = null,
    val fontSize: String? = null,
    val fontSizeAdjust: String? = null,
    val fontStretch: String? = null,
    val fontStyle: String? = null,
    val fontVariant: String? = null,
    val fontWeight: String? = null,
    val height: String? = null,
    val imeMode: String? = null,
    val includeSource: String? = null,
    val layerBackgroundColor: String? = null,
    val layerBackgroundImage: String? = null,
    val layoutFlow: String? = null,
    val layoutGrid: String? = null,
    val layoutGridChar: String? = null,
    val layoutGridCharSpacing: String? = null,
    val layoutGridLine: String? = null,
    val layoutGridMode: String? = null,
    val layoutGridType: String? = null,
    val left: String? = null,
    val letterSpacing: String? = null,
    val lineBreak: String? = null,
    val lineHeight: String? = null,
    val listStyle: String? = null,
    val listStyleImage: String? = null,
    val listStylePosition: String? = null,
    val listStyleType: String? = null,
    val margin: String? = null,
    val marginBottom: String? = null,
    val marginLeft: String? = null,
    val marginRight: String? = null,
    val marginTop: String? = null,
    val markerOffset: String? = null,
    val marks: String? = null,
    val maxHeight: String? = null,
    val maxWidth: String? = null,
    val minHeight: String? = null,
    val minWidth: String? = null,
    val orphans: String? = null,
    val outline: String? = null,
    val outlineColor: String? = null,
    val outlineStyle: String? = null,
    val outlineWidth: String? = null,
    val overflow: String? = null,
    val overflowX: String? = null,
    val overflowY: String? = null,
    val padding: String? = null,
    val paddingBottom: String? = null,
    val paddingLeft: String? = null,
    val paddingRight: String? = null,
    val paddingTop: String? = null,
    val page: String? = null,
    val pageBreakAfter: String? = null,
    val pageBreakBefore: String? = null,
    val pageBreakInside: String? = null,
    val pause: String? = null,
    val pauseAfter: String? = null,
    val pauseBefore: String? = null,
    val pitch: String? = null,
    val pitchRange: String? = null,
    val playDuring: String? = null,
    val position: String? = null,
    val quotes: String? = null,
    val richness: String? = null,
    val right: String? = null,
    val rubyAlign: String? = null,
    val rubyOverhang: String? = null,
    val rubyPosition: String? = null,
    val size: String? = null,
    val speak: String? = null,
    val speakHeader: String? = null,
    val speakNumeral: String? = null,
    val speakPunctuation: String? = null,
    val speechRate: String? = null,
    val stress: String? = null,
    val scrollbarArrowColor: String? = null,
    val scrollbarBaseColor: String? = null,
    val scrollbarDarkShadowColor: String? = null,
    val scrollbarFaceColor: String? = null,
    val scrollbarHighlightColor: String? = null,
    val scrollbarShadowColor: String? = null,
    val scrollbar3dLightColor: String? = null,
    val scrollbarTrackColor: String? = null,
    val tableLayout: String? = null,
    val textAlign: String? = null,
    val textAlignLast: String? = null,
    val textDecoration: String? = null,
    val textIndent: String? = null,
    val textJustify: String? = null,
    val textOverflow: String? = null,
    val textShadow: String? = null,
    val textTransform: String? = null,
    val textAutospace: String? = null,
    val textKashidaSpace: String? = null,
    val textUnderlinePosition: String? = null,
    val top: String? = null,
    val unicodeBidi: String? = null,
    val verticalAlign: String? = null,
    val visibility: String? = null,
    val voiceFamily: String? = null,
    val volume: String? = null,
    val whiteSpace: String? = null,
    val widows: String? = null,
    val width: String? = null,
    val wordBreak: String? = null,
    val wordSpacing: String? = null,
    val wordWrap: String? = null,
    val writingMode: String? = null,
    val zIndex: String? = null,
    val zoom: String? = null
) {
    fun render(): String {
        return buildString {
            accelerator?.let {append("accelerator: $it;")}
            azimuth?.let {append("azimuth: $it;")}
            background?.let {append("background: $it;")}
            backgroundAttachment?.let {append("background-attachment: $it;")}
            backgroundColor?.let {append("background-color: $it;")}
            backgroundImage?.let {append("background-image: $it;")}
            backgroundPosition?.let {append("background-position: $it;")}
            backgroundPositionX?.let {append("background-position-x: $it;")}
            backgroundPositionY?.let {append("background-position-y: $it;")}
            backgroundRepeat?.let {append("background-repeat: $it;")}
            behavior?.let {append("behavior: $it;")}
            border?.let {append("border: $it;")}
            borderBottom?.let {append("border-bottom: $it;")}
            borderBottomColor?.let {append("border-bottom-color: $it;")}
            borderBottomStyle?.let {append("border-bottom-style: $it;")}
            borderBottomWidth?.let {append("border-bottom-width: $it;")}
            borderCollapse?.let {append("border-collapse: $it;")}
            borderColor?.let {append("border-color: $it;")}
            borderLeft?.let {append("border-left: $it;")}
            borderLeftColor?.let {append("border-left-color: $it;")}
            borderLeftStyle?.let {append("border-left-style: $it;")}
            borderLeftWidth?.let {append("border-left-width: $it;")}
            borderRight?.let {append("border-right: $it;")}
            borderRightColor?.let {append("border-right-color: $it;")}
            borderRightStyle?.let {append("border-right-style: $it;")}
            borderRightWidth?.let {append("border-right-width: $it;")}
            borderSpacing?.let {append("border-spacing: $it;")}
            borderStyle?.let {append("border-style: $it;")}
            borderTop?.let {append("border-top: $it;")}
            borderTopColor?.let {append("border-top-color: $it;")}
            borderTopStyle?.let {append("border-top-style: $it;")}
            borderTopWidth?.let {append("border-top-width: $it;")}
            borderWidth?.let {append("border-width: $it;")}
            bottom?.let {append("bottom: $it;")}
            captionSide?.let {append("caption-side: $it;")}
            clear?.let {append("clear: $it;")}
            clip?.let {append("clip: $it;")}
            color?.let {append("color: $it;")}
            content?.let {append("content: $it;")}
            counterIncrement?.let {append("counter-increment: $it;")}
            counterReset?.let {append("counter-reset: $it;")}
            cue?.let {append("cue: $it;")}
            cueAfter?.let {append("cue-after: $it;")}
            cueBefore?.let {append("cue-before: $it;")}
            cursor?.let {append("cursor: $it;")}
            direction?.let {append("direction: $it;")}
            display?.let {append("display: $it;")}
            elevation?.let {append("elevation: $it;")}
            emptyCells?.let {append("empty-cells: $it;")}
            filter?.let {append("filter: $it;")}
            float?.let {append("float: $it;")}
            font?.let {append("font: $it;")}
            fontFamily?.let {append("font-family: $it;")}
            fontSize?.let {append("font-size: $it;")}
            fontSizeAdjust?.let {append("font-size-adjust: $it;")}
            fontStretch?.let {append("font-stretch: $it;")}
            fontStyle?.let {append("font-style: $it;")}
            fontVariant?.let {append("font-variant: $it;")}
            fontWeight?.let {append("font-weight: $it;")}
            height?.let {append("height: $it;")}
            imeMode?.let {append("ime-mode: $it;")}
            includeSource?.let {append("include-source: $it;")}
            layerBackgroundColor?.let {append("layer-background-color: $it;")}
            layerBackgroundImage?.let {append("layer-background-image: $it;")}
            layoutFlow?.let {append("layout-flow: $it;")}
            layoutGrid?.let {append("layout-grid: $it;")}
            layoutGridChar?.let {append("layout-grid-char: $it;")}
            layoutGridCharSpacing?.let {append("layout-grid-char-spacing: $it;")}
            layoutGridLine?.let {append("layout-grid-line: $it;")}
            layoutGridMode?.let {append("layout-grid-mode: $it;")}
            layoutGridType?.let {append("layout-grid-type: $it;")}
            left?.let {append("left: $it;")}
            letterSpacing?.let {append("letter-spacing: $it;")}
            lineBreak?.let {append("line-break: $it;")}
            lineHeight?.let {append("line-height: $it;")}
            listStyle?.let {append("list-style: $it;")}
            listStyleImage?.let {append("list-style-image: $it;")}
            listStylePosition?.let {append("list-style-position: $it;")}
            listStyleType?.let {append("list-style-type: $it;")}
            margin?.let {append("margin: $it;")}
            marginBottom?.let {append("margin-bottom: $it;")}
            marginLeft?.let {append("margin-left: $it;")}
            marginRight?.let {append("margin-right: $it;")}
            marginTop?.let {append("margin-top: $it;")}
            markerOffset?.let {append("marker-offset: $it;")}
            marks?.let {append("marks: $it;")}
            maxHeight?.let {append("max-height: $it;")}
            maxWidth?.let {append("max-width: $it;")}
            minHeight?.let {append("min-height: $it;")}
            minWidth?.let {append("min-width: $it;")}
            orphans?.let {append("orphans: $it;")}
            outline?.let {append("outline: $it;")}
            outlineColor?.let {append("outline-color: $it;")}
            outlineStyle?.let {append("outline-style: $it;")}
            outlineWidth?.let {append("outline-width: $it;")}
            overflow?.let {append("overflow: $it;")}
            overflowX?.let {append("overflow-x: $it;")}
            overflowY?.let {append("overflow-y: $it;")}
            padding?.let {append("padding: $it;")}
            paddingBottom?.let {append("padding-bottom: $it;")}
            paddingLeft?.let {append("padding-left: $it;")}
            paddingRight?.let {append("padding-right: $it;")}
            paddingTop?.let {append("padding-top: $it;")}
            page?.let {append("page: $it;")}
            pageBreakAfter?.let {append("page-break-after: $it;")}
            pageBreakBefore?.let {append("page-break-before: $it;")}
            pageBreakInside?.let {append("page-break-inside: $it;")}
            pause?.let {append("pause: $it;")}
            pauseAfter?.let {append("pause-after: $it;")}
            pauseBefore?.let {append("pause-before: $it;")}
            pitch?.let {append("pitch: $it;")}
            pitchRange?.let {append("pitch-range: $it;")}
            playDuring?.let {append("play-during: $it;")}
            position?.let {append("position: $it;")}
            quotes?.let {append("quotes: $it;")}
            richness?.let {append("richness: $it;")}
            right?.let {append("right: $it;")}
            rubyAlign?.let {append("ruby-align: $it;")}
            rubyOverhang?.let {append("ruby-overhang: $it;")}
            rubyPosition?.let {append("ruby-position: $it;")}
            size?.let {append("size: $it;")}
            speak?.let {append("speak: $it;")}
            speakHeader?.let {append("speak-header: $it;")}
            speakNumeral?.let {append("speak-numeral: $it;")}
            speakPunctuation?.let {append("speak-punctuation: $it;")}
            speechRate?.let {append("speech-rate: $it;")}
            stress?.let {append("stress: $it;")}
            scrollbarArrowColor?.let {append("scrollbar-arrow-color: $it;")}
            scrollbarBaseColor?.let {append("scrollbar-base-color: $it;")}
            scrollbarDarkShadowColor?.let {append("scrollbar-dark-shadow-color: $it;")}
            scrollbarFaceColor?.let {append("scrollbar-face-color: $it;")}
            scrollbarHighlightColor?.let {append("scrollbar-highlight-color: $it;")}
            scrollbarShadowColor?.let {append("scrollbar-shadow-color: $it;")}
            scrollbar3dLightColor?.let {append("scrollbar-3d-light-color: $it;")}
            scrollbarTrackColor?.let {append("scrollbar-track-color: $it;")}
            tableLayout?.let {append("table-layout: $it;")}
            textAlign?.let {append("text-align: $it;")}
            textAlignLast?.let {append("text-align-last: $it;")}
            textDecoration?.let {append("text-decoration: $it;")}
            textIndent?.let {append("text-indent: $it;")}
            textJustify?.let {append("text-justify: $it;")}
            textOverflow?.let {append("text-overflow: $it;")}
            textShadow?.let {append("text-shadow: $it;")}
            textTransform?.let {append("text-transform: $it;")}
            textAutospace?.let {append("text-autospace: $it;")}
            textKashidaSpace?.let {append("text-kashida-space: $it;")}
            textUnderlinePosition?.let {append("text-underline-position: $it;")}
            top?.let {append("top: $it;")}
            unicodeBidi?.let {append("unicode-bidi: $it;")}
            verticalAlign?.let {append("vertical-align: $it;")}
            visibility?.let {append("visibility: $it;")}
            voiceFamily?.let {append("voice-family: $it;")}
            volume?.let {append("volume: $it;")}
            whiteSpace?.let {append("white-space: $it;")}
            widows?.let {append("widows: $it;")}
            width?.let {append("width: $it;")}
            wordBreak?.let {append("word-break: $it;")}
            wordSpacing?.let {append("word-spacing: $it;")}
            wordWrap?.let {append("word-wrap: $it;")}
            writingMode?.let {append("writing-mode: $it;")}
            zIndex?.let {append("z-index: $it;")}
            zoom?.let {append("zoom: $it;")}
        }
    }
}

