package com.instructor.manito

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.instructor.manito.databinding.FragmentAdminRoomBinding
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault

class AdminRoomFragment private constructor() : Fragment() {

    var roomNumber: Int by arg()
    var roomTitle: String by arg()
    var roomPassword: String by argOrDefault("")
    var nickname: String by arg()


    private val whiteGrayInt by lazy {
        ContextCompat.getColor(requireContext(), R.color.whiteGray)
    }

    private val whiteInt by lazy {
        ContextCompat.getColor(requireContext(), R.color.white)
    }

    private lateinit var binding: FragmentAdminRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminRoomBinding.inflate(inflater, container, false)
        binding.run {
            sendButton.isEnabled = false
            roomNumberText.text = roomNumber.toString()
            titleText.text = roomTitle
            passwordText.text = roomPassword
        }


        binding.chatEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.run {
                    // 채팅 메시지가 비어있으면 전송 버튼 비활성화
                    binding.sendButton.isEnabled = if (toString().isEmpty()) {
                        binding.sendButton.setColorFilter(whiteGrayInt)
                        false
                    }
                    // 채팅 메시지가 있으면 전송 버튼 활성화
                    else {
                        binding.sendButton.setColorFilter(whiteInt)
                        true
                    }
                }
            }

        })
        binding.sendButton.setOnClickListener {
            Log.d("jinha", nickname)
        }

        return binding.root
    }

    fun change() {
        nickname = "누구"
    }

    companion object {
        @JvmStatic
        fun newInstance(roomNumber: Int, roomTitle: String, roomPassword: String?, nickname: String) =
            AdminRoomFragment().apply {
                this.roomNumber = roomNumber
                this.roomTitle = roomTitle
                roomPassword?.let { this.roomPassword = it.toString() }
                this.nickname = nickname
            }
    }
}




