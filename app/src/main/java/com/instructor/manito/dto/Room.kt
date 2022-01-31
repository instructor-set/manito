package com.instructor.manito.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Room(
    var rid: String? = null,
    var title: String? = null,
    var password: String? = null,
    var maxUsers: Int? = 0,
    var users: Map<String, String>? = null,
    var manager: String? = null,
    var state: String? = STATE_WAIT,
    var missions: List<String>? = null
) :
    Parcelable {
    companion object {
        const val STATE_WAIT = "WAIT"
        const val STATE_READY = "READY"
        const val STATE_START = "START"
    }
}

