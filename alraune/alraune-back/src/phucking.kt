package alraune.back

import vgrechka.*

fun phucking1() {
    DBPile.init()
    DBPile.execute(alUserRepo.dropTableDDL)
    DBPile.execute(alUserRepo.createTableDDL)

    DBPile.tx {
        val fucko = newAlUser(
            firstName = "Fucko",
            lastName = "Pidoracko",
            email = "fucko@pidoracko.net",
            passwordHash = "bloody-secret",
            sessionID = "adsfasldkfnsadfaldf",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = "We don't tolerate any pidars in our community",
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
        val savedFucko = alUserRepo.insert(fucko)
        println("savedFucko = " + savedFucko)

        val fucko2 = newAlUser(
            firstName = "Fucko 2",
            lastName = "Pidoracko",
            email = "fucko2@pidoracko.net",
            passwordHash = "bloody-secret-2",
            sessionID = "cvxczvurrfasdf",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = null,
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
        val savedFucko2 = alUserRepo.insert(fucko2)
        println("savedFucko2 = " + savedFucko2)
    }

    try {
        DBPile.tx {
            val fucko3 = newAlUser(
                firstName = "Fucko 3",
                lastName = "Pidoracko",
                email = "fucko3@pidoracko.net",
                passwordHash = "bloody-secret-3",
                sessionID = "vkjxzvxvzxcvzxcv",
                profilePhone = "911",
                aboutMe = "I am not pidar, it's just a name",
                adminNotes = "Pidar. Definitely",
                profileRejectionReason = "We don't tolerate any pidars in our community",
                banReason = "Achtung",
                subscribedToAllCategories = true
            )
            val savedFucko3 = alUserRepo.insert(fucko3)
            println("savedFucko3 = " + savedFucko3)
            throw Exception("pizdets")
        }
    } catch (e: Throwable) {
        println("Caught exception: " + e.message)
    }

    DBPile.tx {
        val fucko4 = newAlUser(
            firstName = "Fucko 4",
            lastName = "Pidoracko",
            email = "fucko4@pidoracko.net",
            passwordHash = "bloody-secret-4",
            sessionID = "vxzcvrea43434r",
            profilePhone = "911",
            aboutMe = "I am not pidar, it's just a name",
            adminNotes = "Pidar. Definitely",
            profileRejectionReason = "We don't tolerate any pidars in our community",
            banReason = "Achtung",
            subscribedToAllCategories = true
        )
        val savedFucko4 = alUserRepo.insert(fucko4)
        println("savedFucko4 = " + savedFucko4)
    }

    println("\n----- Select all -----\n")
    run {
        val rows = DBPile.query("select alUser_firstName, alUser_lastName from alraune_users;")
        for ((index, row) in rows.withIndex()) {
            println("${index + 1}) ${row[0]}    ${row[1]}")
        }
    }

    println("\n----- With specific secret -----\n")
    run {
        val items = alUserRepo.select(AlUser::passwordHash, DBPile.op.eq, "bloody-secret-2")
        for ((index, item) in items.withIndex()) {
            println("${index + 1})")
            println("    id = ${item.id}")
            println("    createdAt = ${item.createdAt.toString()}")
            println("    updatedAt = ${item.updatedAt.toString()}")
            println("    deleted = ${item.deleted}")
            println("    firstName = ${item.firstName}")
            println("    lastName = ${item.lastName}")
            println("    email = ${item.email}")
            println("    passwordHash = ${item.passwordHash}")
            println("    profilePhone = ${item.profilePhone}")
            println("    adminNotes = ${item.adminNotes}")
            println("    aboutMe = ${item.aboutMe}")
            println("    profileRejectionReason = ${item.profileRejectionReason}")
            println("    banReason = ${item.banReason}")
            println("    subscribedToAllCategories = ${item.subscribedToAllCategories}")
        }
    }

    println("\nOK")
}

