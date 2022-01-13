package com.instructor.manito.dto

data class Token(
    val kakaoAccessToken: String? = null,
    val serverAccessToken: String? = null,
    val firebaseCustomAuthToken: String? = null
)
