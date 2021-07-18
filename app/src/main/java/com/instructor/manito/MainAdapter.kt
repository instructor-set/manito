package com.instructor.manito

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.FragmentAdminRoomBinding
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrDefault

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
data class Room(var no: Int?, var title: String, var roomPassword:Int?) {
    constructor(title: String) : this(null, title, null)
}
