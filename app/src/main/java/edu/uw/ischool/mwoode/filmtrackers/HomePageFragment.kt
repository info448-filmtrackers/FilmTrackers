package edu.uw.ischool.mwoode.filmtrackers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomePageFragment : Fragment() {
    private lateinit var userHistoryHomepageCard: Button
    private lateinit var searchHomepageCard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userHistoryHomepageCard = view.findViewById(R.id.userHistoryHomepageCard) as Button
        searchHomepageCard = view.findViewById(R.id.searchHomepageCard) as Button

        userHistoryHomepageCard.setOnClickListener {
            navigateToFragment(R.id.userHistory)
        }

        searchHomepageCard.setOnClickListener {
            navigateToFragment(R.id.search)
        }
    }

    private fun navigateToFragment(itemId: Int) {
        (activity as MainActivity).switchToFragment(itemId)

        // update navbar on change
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.selectedItemId = itemId
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomePageFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}