package fekjs

import fekjs.node.*
import org.w3c.dom.HTMLElement

external interface ReactElement
external interface ReactClass

external object ReactDOM {
    fun render(rel: ReactElement?, container: HTMLElement)
    fun unmountComponentAtNode(container: HTMLElement)
}

external object React {
    fun createElement(tag: String, attrs: Json, vararg children: ReactElement): ReactElement
    fun createElement(clazz: ReactClass, attrs: Json, vararg children: ReactElement): ReactElement
    fun createClass(spec: dynamic): ReactClass
}

fun initReactNode() {
    global.ReactDOM = nodeRequire("react-dom")
    global.React = nodeRequire("react")
}

