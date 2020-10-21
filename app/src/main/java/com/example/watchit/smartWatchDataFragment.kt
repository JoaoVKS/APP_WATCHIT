package com.example.watchit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_DEVICE_NAME = "device_name"
private const val ARG_USER_ID = "user_id"

/**
 * A simple [Fragment] subclass.
 * Use the [smartWatchDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class smartWatchDataFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var device_name: String? = null
    private var user_id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            device_name = it.getString(ARG_DEVICE_NAME)
            user_id = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this

        //AQUI


        return inflater.inflate(R.layout.fragment_smart_watch_data, container, false)
    }

    companion object {
        /**
         * @param device_name Parameter 1.
         * @param user_id Parameter 2.
         * @return A new instance of fragment smartWatchDataFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(device_name: String, user_id: String) =
            smartWatchDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEVICE_NAME, device_name)
                    putString(ARG_USER_ID, user_id)
                }

            }
    }
}