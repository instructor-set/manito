package com.instructor.manito

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellMainBinding
import com.instructor.manito.dto.Room

class MainAdapter(private val context: Context, private val listData: ArrayList<Room>) :
    RecyclerView.Adapter<MainAdapter.Holder>() {

    interface OnItemClickListener {
        fun onItemClick(v: View, data: Room, pos: Int)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.Holder {
        val view = CellMainBinding.inflate(LayoutInflater.from(context), parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MainAdapter.Holder, position: Int) {
        val room: Room = listData[position]
        //Log.e("dataList", "data : $listData")
        holder.binding(room)
    }

    override fun getItemCount(): Int {
        return listData.size
        //Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(private val bind: CellMainBinding) : RecyclerView.ViewHolder(bind.root) {
        fun binding(room: Room) {

            with(bind) {
                cellTitleText.text = room.title
                @SuppressLint("SetTextI18n")
                cellNumberOfPeople.text = "${room.users?.size}/${room.maxUsers}"
                if (room.password.isNullOrBlank()) {
                    cellKeyImage.visibility = View.INVISIBLE
                }

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    itemView.setOnClickListener {
                        listener?.onItemClick(itemView, room, pos)
                    }
                }
            }


        }

    }

}