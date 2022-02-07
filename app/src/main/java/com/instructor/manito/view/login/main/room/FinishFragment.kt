package com.instructor.manito.view.login.main.room

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.FragmentFinishBinding
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
                    Util.uidToNickname(it.getValue<String>()!!) { myManito ->
                        finishText2.text = "당신은 ${myManito}의 마니또였습니다."
                        Database.getReference("games/${rid}/$it/manito").get()
                            .addOnSuccessListener { that ->
                                Util.uidToNickname(that.getValue<String>()!!) { maManito ->
                                    finishText3.text = "${myManito}의 마니또는 ${maManito}였습니다."
                                }
                            }
                    }
                }



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
}