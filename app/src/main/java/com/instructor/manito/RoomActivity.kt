package com.instructor.manito

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.instructor.manito.databinding.ActivityRoomBinding
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Util
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras

class RoomActivity : AppCompatActivity() {

    object Extras: BundleSpec() {
        var room: Room by bundle()
    }
    private val room by lazy {
        withExtras(Extras) {
            room
        }
    }

    private val bind by lazy {
        ActivityRoomBinding.inflate(layoutInflater)
    }

    private val whiteGrayInt by lazy {
        ContextCompat.getColor(this@RoomActivity, R.color.whiteGray)
    }

    private val whiteInt by lazy {
        ContextCompat.getColor(this@RoomActivity, R.color.white)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)



        bind.apply {
            sendButton.isEnabled = false
            roomNumberText.text = room.no.toString()
            titleText.text = room.title
            passwordText.text = room.password
            chatEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    p0?.run {
                        // 채팅 메시지가 비어있으면 전송 버튼 비활성화
                        sendButton.isEnabled = if (toString().isEmpty()) {
                            sendButton.setColorFilter(whiteGrayInt)
                            false
                        }
                        // 채팅 메시지가 있으면 전송 버튼 활성화
                        else {
                            sendButton.setColorFilter(whiteInt)
                            true
                        }
                    }
                }

            })
            sendButton.setOnClickListener {
                Util.j("전송됨")
            }
        }
    }
}