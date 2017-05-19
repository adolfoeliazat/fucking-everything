package alraune.shared

object AlSharedPile {
    object dom {
        object id {
            val googleSignInButton = "googleSignInButton"
            val facebookSignInButton = "facebookSignInButton"
        }
    }

    object pageID {
        val landing = "landing"
        val order = "order"
    }

    enum class GetParam {
        page, fakeSetting
    }

    enum class Cookie {
        sessionID
    }
}

class ShitPassedFromBackToFront(
    val pageID: String
)


