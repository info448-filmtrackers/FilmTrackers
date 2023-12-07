package edu.uw.ischool.mwoode.filmtrackers

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TITLE = "title"

/**
 * A simple [Fragment] subclass.
 * Use the [FilterButton.newInstance] factory method to
 * create an instance of this fragment.
 */
class FilterButton : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null;
    private lateinit var button: Button;
    private var selected: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
        }
    }

    fun onClick(callback: () -> Unit) {
        button.setOnClickListener{
            activity?.runOnUiThread {
                selected = !selected
                if (selected) {
                    button.background.setTint(Color.BLACK)
                } else {
                    button.background.setTint(Color.rgb(94, 63, 186))
                }
            }
            callback()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_filter_button, container, false)
        button = root.findViewById<Button>(R.id.filterButton)
        button.text = title

        return root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FilterButton.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(title: String) =
            FilterButton().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
    }
}