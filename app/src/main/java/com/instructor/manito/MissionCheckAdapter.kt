package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.AlertdialogEdittextBinding
import com.instructor.manito.databinding.AlertdialogEnterRoomBinding
import com.instructor.manito.databinding.CellMissionCheckBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.bundle.putExtras

class MissionCheckAdapter(private val context: Context, private var listData: HashMap<String, Boolean>) :
    RecyclerView.Adapter<MissionCheckAdapter.Holder>() {

    val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionCheckAdapter.Holder {
        val view = CellMissionCheckBinding.inflate(inflater, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MissionCheckAdapter.Holder, position: Int) {

     }

    override fun getItemCount(): Int {
        return listData.size
        //Log.e("dataList", "size : ${listData.size}")
    }


    inner class Holder(private val bind: CellMissionCheckBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {


            }


        }

    }

}