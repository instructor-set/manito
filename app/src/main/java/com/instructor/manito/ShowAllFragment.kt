package com.instructor.manito

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instructor.manito.databinding.CellResultBinding
import com.instructor.manito.databinding.FragmentShowAllBinding
import com.instructor.manito.lib.Util
import com.instructor.manito.view.login.main.RoomActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShowAllFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShowAllFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var roomActivity: RoomActivity? = null

    // 마니또 클래스
    data class Manito(
        val me: String = "",
        val you: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        roomActivity = context as RoomActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val bind = FragmentShowAllBinding.inflate(inflater, container, false)
        with(bind){

            // 총 몇명
            showAllText1.text = "총 " + "10"


            // recyclerview
            val listData = arrayListOf<Manito>()
            val resultRecyclerAdapter = ResultRecylerAdpater(listData)

            // 데이터 넣기
            listData.add(Manito("나", "너"))
            listData.add(Manito("사람2", "나"))
            listData.add(Manito("너", "사람2"))
            //Util.j(listData)
            resultRecycler.adapter = resultRecyclerAdapter

            return root
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ShowAllFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowAllFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // adapter
    inner class ResultRecylerAdpater(
        private var listData: ArrayList<Manito>
    ) :
        RecyclerView.Adapter<ResultRecylerAdpater.ViewHolder>() {

        val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultRecylerAdpater.ViewHolder {
            val view =
                CellResultBinding.inflate(inflater, parent, false)
            Util.j(listData)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ResultRecylerAdpater.ViewHolder, position: Int) {
            val manito: Manito = listData[position]

            holder.binding(manito)
        }

        override fun getItemCount(): Int {
            return listData.size
        }

        inner class ViewHolder(private val bind: CellResultBinding) :
            RecyclerView.ViewHolder(bind.root) {
            fun binding(manito:Manito) {
                with(bind) {
                    Util.j(listData)
                    person1.text = manito.me
                    person2.text = manito.you


                }

            }

        }
    }
}