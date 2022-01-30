package com.instructor.manito

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellMissionCheckBinding
import com.instructor.manito.lib.Util

class MissionCheckAdapter(private val context: Context, private var listData: List<String>) :
    RecyclerView.Adapter<MissionCheckAdapter.Holder>() {

    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionCheckAdapter.Holder {
        val view = CellMissionCheckBinding.inflate(inflater, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MissionCheckAdapter.Holder, position: Int) {
        holder.binding(listData[position])
     }

    override fun getItemCount(): Int {
        return listData.size
    }


    inner class Holder(private val bind: CellMissionCheckBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(mission: String) {

            with(bind) {
                cellMissionText.text = mission

                missionCheckBox.setOnCheckedChangeListener { compoundButton, b ->
                    Util.j(missionCheckBox.isChecked)
                }
            }


        }

    }

}