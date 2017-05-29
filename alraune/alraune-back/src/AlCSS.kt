package alraune.back

import alraune.shared.AlSharedPile
import alraune.shared.Color
import vgrechka.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object AlCSS {
    data class Pack(
        val default: String? = null,
        val link: String? = null,
        val visited: String? = null,
        val hover: String? = null,
        val active: String? = null,
        val focus: String? = null,
        val hoverActive: String? = null,
        val hoverFocus: String? = null,
        val firstChild: String? = null,
        val notFirstChild: String? = null,
        val forcedClassName: String? = null
    ) {
        var autoClassName: String? = null

        val className get() =
            forcedClassName
            ?: autoClassName
            ?: wtf("41ff0be5-119a-4a7f-b46e-151980db3b67")

        override fun toString() = className
    }


    object carla {
        val title = Pack("""
background-color: #eceff1;
    font-size: 125%;
    margin-bottom: 0.5rem;
    position: relative;
    padding-right: 55px;
    padding-left: 24px;
        """)

        val titleIcon = Pack("""
position: absolute;
    left: 4px;
    top: 4px;
color: #90a4ae;
""")

        val body = Pack("""
    background-color: #ffffff;
    margin-bottom: 1rem;
        """)

        val titleRightIcons = Pack("""
    position: absolute;
    right: 4px;
    top: 0;
        """)

        val titleRightIcon = Pack(
            default = """
        margin-left: 1rem;
        color: #90a4ae;
                """,
            hover = """
        color: #546e7a;
        cursor: pointer;
            """)

        val tinySubtitle = Pack("""
            font-size: 75%;
            color: #757575;
            margin-left: 1rem;
"""
        )
    }

    val errorBanner = Pack("""
        background-color: ${Color.RED_50};
        border-left: 3px solid ${Color.RED_300};
        margin-bottom: 1.5rem;
        padding-left: 1rem;
        padding-top: 1rem;
        padding-bottom: 1rem;""")

    val successBanner = Pack("""
        background-color: ${Color.GREEN_50};
        border-left: 3px solid ${Color.GREEN_300};
        margin-bottom: 1.5rem;
        padding-left: 1rem;
        padding-top: 1rem;
        padding-bottom: 1rem;""")

    val submitForReviewBanner = Pack("""
        background-color: #eceff1;
        border-left: 3px solid #90a4ae;
        margin-bottom: 1rem;
        padding-left: 1rem;
        padding-right: 0;
        padding-top: 0.5rem;
        padding-bottom: 0.5rem;
        display: flex;
        align-items: center;""")

    val deleteItemModalBodySubtitle = Pack("""
margin-bottom: 1rem;
    font-weight: bold;
    """)

    val ticker = Pack("""
            display: none;
            width: 14px;
            background-color: ${Color.BLUE_GRAY_600};
            height: 34px;
            float: right;
            animation-name: ticker;
            animation-duration: 500ms;
            animation-iteration-count: infinite;
    """)

    val fuckAway = Pack("""
            animation-name: fuckAway;
            animation-duration: 500ms;
            animation-iteration-count: 1;
    """, forcedClassName = AlSharedPile.className.fuckAway)

    val sheet = run {
        val buf = StringBuilder()

        buf.append("""
            body {overflow-x: hidden; overflow-y: scroll; padding-right: 0px !important;}

            .modal-open .modal {
                overflow-x: hidden;
                overflow-y: scroll;
            }

            .modal-backdrop {
                overflow-x: hidden;
                overflow-y: scroll;
            }

            .modal-footer .btn+.btn {
                margin-left: 8px;
            }

            .${AlSharedPile.className.paddingRightScrollbarWidthImportant} {
                padding-right: ${AlSharedPile.scrollbarWidth}px !important;
            }

            button:disabled {cursor: default !important;}
            input:disabled {cursor: default !important;}
            textarea:disabled {cursor: default !important;}
            select:disabled {cursor: default !important;}

            .form-control:focus {border-color: #b0bec5; box-shadow: inset 0 1px 1px rgba(0,0,0,.075),0 0 8px rgba(176,190,197,.6);}

            .btn-primary {background-color: #78909c; border-color: #546e7a;}
            .btn-primary:hover {background-color: #546e7a; border-color: #37474f;}
            .btn-primary:focus {background-color: #455a64; border-color: #263238; outline-color: #b0bec5;}
            .btn-primary:focus:hover {background-color: #455a64; border-color: #263238;}
            .btn-primary:active {background-color: #455a64; border-color: #263238;}
            .btn-primary:active:focus {background-color: #455a64; border-color: #263238; outline-color: #b0bec5;}
            .btn-primary:active:hover {background-color: #455a64; border-color: #263238;}

            .btn-primary.disabled.focus,
            .btn-primary.disabled:focus,
            .btn-primary.disabled:hover,
            .btn-primary[disabled].focus,
            .btn-primary[disabled]:focus,
            .btn-primary[disabled]:hover,
            fieldset[disabled] .btn-primary.focus,
            fieldset[disabled] .btn-primary:focus,
            fieldset[disabled] .btn-primary:hover {
                background-color: #78909c;
                border-color: #546e7a;
            }

            @keyframes ticker {
                0% {
                    opacity: 1;
                }

                100% {
                    opacity: 0;
                }
            }

            @keyframes fuckAway {
                0% {
                    opacity: 1;
                }

                100% {
                    opacity: 0;
                }
            }

            .btn-default:focus {border-color: #ccc; outline-color: transparent;}
            .btn-default:active {border-color: #ccc; outline-color: transparent;}
            .btn-default:focus:active {border-color: #8c8c8c; outline-color: transparent;}
        """)

        fun fart(clazz: KClass<*>, selectorPrefix: String = "") {
            for (prop in clazz.memberProperties) {
                prop as KProperty1<Any?, Any?>
                if (prop.returnType.classifier == Pack::class) {
                    val pack = prop.get(clazz.objectInstance) as Pack
                    pack.autoClassName = selectorPrefix + prop.name
                    val selector = "." + pack.className
                    pack.default?.let {buf.ln("$selector {$it}")}
                    pack.link?.let {buf.ln("$selector:link {$it}")}
                    pack.visited?.let {buf.ln("$selector:visited {$it}")}
                    pack.hover?.let {buf.ln("$selector:hover {$it}")}
                    pack.active?.let {buf.ln("$selector:active {$it}")}
                    pack.focus?.let {buf.ln("$selector:focus {$it}")}
                    pack.hoverActive?.let {buf.ln("$selector:hover:active {$it}")}
                    pack.hoverFocus?.let {buf.ln("$selector:hover:focus {$it}")}
                    pack.firstChild?.let {buf.ln("$selector:first-child {$it}")}
                    pack.notFirstChild?.let {buf.ln("$selector:nth-child(1n+2) {$it}")}
                }
            }

            for (nestedClass in clazz.nestedClasses) {
                if (nestedClass.objectInstance != null) {
                    fart(nestedClass, "${nestedClass.simpleName}-")
                }
            }
        }

        fart(AlCSS::class)

        buf.toString()
    }
}
