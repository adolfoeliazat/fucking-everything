package alraune.shared

import vgrechka.*

object AlSharedPile {
    object domID {
        val googleSignInButton by myName()
        val facebookSignInButton by myName()
        val createOrderForm_submitButton by myName()
    }

    object pageID {
        val landing by myName()
        val orderCreation by myName()
    }

    enum class GetParam {
        page, fakeSetting
    }

    enum class Cookie {
        sessionID
    }
}

@Ser data class OrderCreationForm(
    val email: String,
    val name: String,
    val phone: String,
    val documentTitle: String,
    val documentDetails: String
)

class ShitPassedFromBackToFront(
    val pageID: String
)



















