package com.instructor.manito.lib

import com.google.firebase.auth.FirebaseAuth
import com.instructor.manito.dto.User

object Authentication {

    val uid
        get() = FirebaseAuth.getInstance().currentUser?.uid
    var user: User? = null


}