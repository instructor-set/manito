package com.instructor.manito.view.login.main.room

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.FragmentFinishBinding
import com.instructor.manito.databinding.LayoutMissionBinding
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import com.instructor.manito.view.login.main.RoomActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinishFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinishFragment : Fragment() {


    var rid: String? = null

    var roomActivity: RoomActivity? = null

    data class Mission(
        val title: String = "",
        var result: Boolean = false,
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        roomActivity = context as RoomActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        var bind = FragmentFinishBinding.inflate(inflater, container, false)
        with(bind) {
            finishShowAllButton.setOnClickListener {
                roomActivity!!.setFragment(true)
            }
            Database.getReference("games/${rid}/${Authentication.uid}/manito").get()
                .addOnSuccessListener {
                    val myManitoId = it.getValue<String>()!!
                    Util.uidToNickname(myManitoId) { myManito ->
                        finishText2.text = "당신은 ${myManito}의 마니또입니다."
                        Database.getReference("games/${rid}/$myManitoId/manito").get()
                            .addOnSuccessListener { that ->
                                Util.uidToNickname(that.getValue<String>()!!) { maManito ->
                                    finishText3.text = "${myManito}의 마니또는 ${maManito}였습니다."
                                }
                            }
                    }
                }

            val missionList = arrayListOf<Mission>()
            val finishMissionAdapter = FinishMissionAdapter(missionList)
            Database.getReference("games/$rid/${Authentication.uid}/missions")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        missionList.add(Mission(snapshot.key!!, snapshot.getValue<Boolean>()!!))
                        finishMissionAdapter.notifyItemInserted(missionList.size)
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {
                        missionList.forEach {
                            if (it.title == snapshot.key) {
                                it.result = snapshot.getValue<Boolean>()!!
                                finishMissionAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            missionRecycler.adapter = finishMissionAdapter
            return root
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(rid: String?) =
            FinishFragment().apply {
                this.rid = rid
            }
    }

    inner class FinishMissionAdapter(
        private var listData: ArrayList<Mission>,
    ) :
        RecyclerView.Adapter<FinishMissionAdapter.ViewHolder>() {

        val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): FinishMissionAdapter.ViewHolder {
            val view =
                LayoutMissionBinding.inflate(inflater, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: FinishMissionAdapter.ViewHolder, position: Int) {
            val mission: Mission = listData[position]
            holder.binding(mission)
        }

        override fun getItemCount(): Int {
            return listData.size
        }

        inner class ViewHolder(private val bind: LayoutMissionBinding) :
            RecyclerView.ViewHolder(bind.root) {
            fun binding(mission: Mission) {
                with(bind) {

                    cellMissionText.text = mission.title
                    cellMissionText.isSelected = true

                    cellMissionTextResult.text =
                        when (mission.result) {
                            true -> " 성공"
                            false -> "실패"
                        }
                }

            }

        }
    }
}