package com.instructor.manito

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.LayoutImportantMessageBinding
import com.instructor.manito.databinding.LayoutLeftMessageBinding
import com.instructor.manito.databinding.LayoutRightMessageBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Util.uidToNickname
import java.text.SimpleDateFormat
import java.util.*

class RoomChatAdapter(private val context: Context, private val chatList: ArrayList<Chat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_LEFT = 0
        private const val VIEW_TYPE_RIGHT = 1
        private const val VIEW_TYPE_ENTER = 2
    }
    private val simpleDateFormat = SimpleDateFormat("a h:mm", Locale.KOREA)

    inner class LeftMessageHolder(private val bind: LayoutLeftMessageBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun binding(chat: Chat) {
            with(bind) {
                messageTextView.text = chat.message
                timestampTextView.text = unixTimeToDateString(chat.timestamp as Long)
                uidToNickname(chat.uid!!) {
                    nicknameTextView.text = it as String
                }
            }
        }

    }

    inner class RightMessageHolder(private val bind: LayoutRightMessageBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun binding(chat: Chat) {
            with(bind) {
                messageTextView.text = chat.message
                timestampTextView.text = unixTimeToDateString(chat.timestamp as Long)
            }
        }

    }

    inner class ImportantMessageHolder(private val bind: LayoutImportantMessageBinding) :
        RecyclerView.ViewHolder(bind.root) {
        fun binding(chat: Chat) {
            with(bind) {
                if (chat.type == Chat.TYPE_ENTER) {
                    uidToNickname(chat.uid!!) {
                        messageTextView1.text = context.getString(R.string.enter_message, it)
                    }
                }
            }
        }
    }

    fun unixTimeToDateString(timestamp: Long): String = simpleDateFormat.format(Date(timestamp))


    override fun getItemViewType(position: Int): Int {
        val chat = chatList[position]
        return if (chat.type == Chat.TYPE_ENTER) {
            VIEW_TYPE_ENTER
        } else {
            when (chat.uid) {
                Authentication.uid -> VIEW_TYPE_RIGHT
                else -> VIEW_TYPE_LEFT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        return when (viewType) {
            VIEW_TYPE_LEFT -> LeftMessageHolder(
                LayoutLeftMessageBinding.inflate(layoutInflater, parent, false)
            )
            VIEW_TYPE_RIGHT -> RightMessageHolder(
                LayoutRightMessageBinding.inflate(layoutInflater, parent, false)
            )
            else -> ImportantMessageHolder(
                LayoutImportantMessageBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]
        when (holder) {
            is LeftMessageHolder -> {
                holder.binding(chat)
            }
            is RightMessageHolder -> {
                holder.binding(chat)
            }
            is ImportantMessageHolder -> {
                holder.binding(chat)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }




}