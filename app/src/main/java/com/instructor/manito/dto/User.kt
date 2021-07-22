package com.instructor.manito.dto

data class User(var nickname: String = "",
                var rooms: HashMap<String, Boolean>? = null)
