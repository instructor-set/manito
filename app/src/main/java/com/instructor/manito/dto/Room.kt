package com.instructor.manito.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Room(var no: Long? = null,
                var title: String? = null,
                var password: String? = null,
                var maxUsers: Int? = 0,
                var users: HashMap<String, Boolean>? = null) :
    Parcelable

