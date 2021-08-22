package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellMissionBinding
import com.instructor.manito.dto.Chat
import com.instructor.manito.dto.Room
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import splitties.activities.start
import splitties.bundle.putExtras
import splitties.toast.toast

class MissionAdapter(private val context: Context, private var listData: ArrayList<String>) :
    RecyclerView.Adapter<MissionAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionAdapter.Holder {
        val view = CellMissionBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MissionAdapter.Holder, position: Int) {
        val mission: String = listData[position]
        //Log.e("dataList", "data : $listData")
        holder.binding(mission)
    }

    override fun getItemCount(): Int {
        return listData.size
        //Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(private val bind: CellMissionBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(mission: String) {

            with(bind) {
                // 미션 입력하고 버튼을 누르면
                // 그다음꺼가 생기고, 기존꺼는 클릭 불가능
                // 버튼 drawable도 달라짐

                missionButton.setOnClickListener {

                }
            }
        }

    }

}
