package com.instructor.manito

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructor.manito.databinding.FragmentFinishBinding
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
        with(bind){
            bind.finishShowAllButton.setOnClickListener {
                roomActivity!!.setFramgent(true)
            }
        }
        return bind.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinishFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinishFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}