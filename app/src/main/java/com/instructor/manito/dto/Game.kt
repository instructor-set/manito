package com.instructor.manito.dto

data class Game(
  var manito: String? = null,
  var missions: HashMap<String, Boolean>? = null
) {
  companion object {
    const val TYPE_MESSAGE = 0
    const val TYPE_ENTER = 1
    const val MESSAGE_ENTER = "_ENTER_MESSAGE"
  }
}