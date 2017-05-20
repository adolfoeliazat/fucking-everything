package alraune.back

import alraune.shared.AlSharedPile
import vgrechka.*

enum class FakeSetting {
    Dunduk1 {
        override fun ignite() {
            return
            DBPile.execute(AlGeneratedDBPile.ddl.dropCreateAllScript)
            alUserRepo.insert(newAlUser(firstName = "Валентин",
                                        lastName = "Пёздолов",
                                        email = "vpezdolov@gmail.com",
                                        passwordHash = "123123123123",
                                        sessionID = "7a7c2f80-905d-45f3-9b51-f10d8193d924",
                                        profilePhone = "+380683425484",
                                        aboutMe = "Я не мудак, вовсе нет. Точно не мудак!",
                                        adminNotes = "Мудак какой-то, похоже",
                                        profileRejectionReason = null,
                                        banReason = null,
                                        subscribedToAllCategories = false))

            AlBackPile.cookies = FakeCookies()-{o->
                o.nameToValue[AlSharedPile.Cookie.sessionID.name] = "some shit 7a7c2f80-905d-45f3-9b51-f10d8193d924"
            }
        }
    };

    abstract fun ignite()
}

