package com.instructor.manito.view.login.main.room

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellMissionCheckBinding
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database

class MissionCheckAdapter(private val context: Context, private var listData: List<String>, private val rid: String) :
    RecyclerView.Adapter<MissionCheckAdapter.Holder>() {

    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = CellMissionCheckBinding.inflate(inflater, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding(listData[position], position)
     }

    override fun getItemCount(): Int {
        return listData.size
    }

    var isGameStart = false



    inner class Holder(private val bind: CellMissionCheckBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(mission: String, position: Int) {

            with(bind) {
                cellMissionText.text = mission
                val ref = Database.getReference("games/$rid/${Authentication.uid}/missions/$mission")

                missionCheckBox.setOnCheckedChangeListener { _, _ ->
                    ref.setValue(missionCheckBox.isChecked)
                }
                if (isGameStart) {
                    missionCheckBox.visibility = View.VISIBLE
                } else {
                    missionCheckBox.visibility = View.GONE
                }
            }


        }

    }

}