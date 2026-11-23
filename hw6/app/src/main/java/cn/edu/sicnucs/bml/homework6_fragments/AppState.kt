package cn.edu.sicnucs.bml.homework6_fragments

import cn.edu.sicnucs.bml.homework6_fragments.model.CardMatchingGame
import java.io.Serializable

interface AppStateHost {
    val appState: AppState
    fun persistState()
}

class AppState(
    var profile: ProfileData = ProfileData(),
    var twoCardGame: CardMatchingGame? = null,
    var threeCardGame: CardMatchingGame? = null,
    var twoCardBest: Int = 0,
    var threeCardBest: Int = 0
) : Serializable

class ProfileData(
    var name: String = "",
    var student: String = "",
    var phone: String = "",
    var email: String = "",
    var gender: Int = 0,
    var hobbies: MutableList<String> = mutableListOf(),
    var avatarFile: String? = null
) : Serializable {
    fun copyFrom(
        name: String,
        student: String,
        phone: String,
        email: String,
        gender: Int,
        hobbies: List<String>
    ): ProfileData {
        return ProfileData(
            name = name,
            student = student,
            phone = phone,
            email = email,
            gender = gender,
            hobbies = hobbies.toMutableList(),
            avatarFile = avatarFile
        )
    }
}
