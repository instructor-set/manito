package com.instructor.manito

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellMissionBinding


class MissionAdapter(private val context: Context, private var listData: ArrayList<String>) :
    RecyclerView.Adapter<MissionAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionAdapter.Holder {
        val view = CellMissionBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MissionAdapter.Holder, position: Int) {
        val mission: String = listData[position]
        holder.binding(mission)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    inner class Holder(private val bind: CellMissionBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(mission: String) {

            with(bind) {
                // 미션 입력하고 버튼을 누르면
                // 그다음꺼가 생기고, 기존꺼는 클릭 불가능
                // 버튼 drawable도 달라짐
                missionEditText.addTextChangedListener {
                    listData[adapterPosition] = it.toString()
                }
                missionEditText.setText(mission)
                missionButton.text = "추가"
                if (adapterPosition == listData.lastIndex) {
                    missionEditText.post {
                        missionEditText.isFocusableInTouchMode = true
                        missionEditText.requestFocus()
                        val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                        imm?.showSoftInput(missionEditText, 0)
                    }
                }


                missionButton.setOnClickListener {
                    if (missionButton.text == "추가") {
                        if (missionEditText.text.isNotEmpty()) {
                            missionButton.text = "삭제"
                            listData.add("")
                            notifyItemInserted(listData.lastIndex)
                        }
                    } else {
                        listData.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                    }

                }

            }


        }

    }

}
