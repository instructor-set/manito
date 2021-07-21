package com.instructor.manito

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.dto.Room

class MainAdapter(val context: Context, val listData: ArrayList<Room>) : RecyclerView.Adapter<MainAdapter.Holder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.cell_main, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: MainAdapter.Holder, position: Int) {
        val room : Room = listData[position]
        Log.e("dataList", "data : $listData")
        holder.bind(room)
    }

    override fun getItemCount(): Int {
        return listData.size
        Log.e("dataList", "size : ${listData.size}")
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var mRoom : Room? = null

        val titleText = itemView?.findViewById<TextView>(R.id.cellTitleText)
        val roomNumberText = itemView?.findViewById<TextView>(R.id.cellRoomNumberText)
        val keyImage = itemView?.findViewById<ImageView>(R.id.cellKeyImage)
        val numberofPeople = itemView?.findViewById<TextView>(R.id.cellNumberOfPeople)


        fun bind(room: Room){
            titleText.text = room.title
            roomNumberText.text = room.no.toString()
            if(room.roomPassword != null){
                keyImage?.setImageResource(R.drawable.ic_baseline_vpn_key_24)
            }else{
                keyImage.visibility = View.INVISIBLE
            }
            this.mRoom = room

        }

    }

}