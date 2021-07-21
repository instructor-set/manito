package com.instructor.manito.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue


@Parcelize
data class Room(var no: Long? = null, var title: String? = null, var password: String? = null, var numberOfPeople: Int? = 0, var participatingUsers:  @RawValue MutableList<User>? = null) :
    Parcelable

