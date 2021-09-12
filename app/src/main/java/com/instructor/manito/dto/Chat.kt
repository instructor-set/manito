package com.instructor.manito.dto

data class Chat(
    var uid: String? = null,
    var type: Int? = null,
    var message: String? = null,
    var timestamp: Any? = null
) {
    companion object {
        const val TYPE_MESSAGE = 0
        const val TYPE_ENTER = 1
        const val TYPE_EXIT = 2
        const val MESSAGE_ENTER = "_ENTER_MESSAGE"
        const val MESSAGE_EXIT = "_EXIT_MESSAGE"
    }
}
