package com.instructor.manito.lib

import com.google.firebase.auth.FirebaseAuth
import com.instructor.manito.dto.User

object Authentication {

    var serverAccessToken: String? = null
        set(value) {
            Util.j("액세스 토큰: $value")
            field = value
        }
    val bearerAccessToken: String
        get() = "Bearer $serverAccessToken"

    val uid
        get() = FirebaseAuth.getInstance().currentUser?.uid
    val nickname
        get() = FirebaseAuth.getInstance().currentUser?.displayName
    var user: User? = null
    fun isLoggedIn() = when (FirebaseAuth.getInstance().currentUser) {
        null -> false
        else -> true
    }


}