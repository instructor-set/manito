package com.instructor.manito.dto

data class User(var nickname: String = "",
                var rooms: Map<String, Any>? = null)