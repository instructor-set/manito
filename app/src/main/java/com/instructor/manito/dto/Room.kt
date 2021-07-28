package com.instructor.manito.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Room(
                var rid: String? = null,
                var title: String? = null,
                var password: String? = null,
                var maxUsers: Int? = 0,
                var users: Map<String, Boolean>? = null,
                var manager: String? = null) :
    Parcelable

