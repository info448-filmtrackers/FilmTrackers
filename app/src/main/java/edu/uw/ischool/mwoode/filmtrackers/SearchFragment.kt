package edu.uw.ischool.mwoode.filmtrackers

import android.R.attr.button
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.Executors


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var tasksShown: Boolean = false
    private var searchInProg: TimerTask? = null
    private var filters: Array<String> = arrayOf("Popular", "Niche", "Highly Rated", "Foreign")
    private val selectedFilters: ArrayList<String> = arrayListOf()
    private var lastSearch: JSONArray? = null
    private var filterButtons: ArrayList<FilterButton> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // double make sure
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    // Searches tmdb for matches
    fun search(query: String) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(getString(R.string.search_url, query)) // args: search query
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                .build()

            val response = client.newCall(request).execute()
            val check = JSONObject(response.body()?.string())["results"]
            lastSearch = JSONArray(check.toString())
            displaySearchResults(JSONArray(check.toString()))
        }
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//
//        super.onSaveInstanceState(outState)
//    }

    fun displaySearchResults(searchResults: JSONArray) {
        Log.i("SEARCH", searchResults.toString())
        Log.i("FILTER", "SEARCH RESULTS UPDATED")

        activity?.runOnUiThread {
            view?.findViewById<LinearLayout>(R.id.searchResultsHolder)?.removeAllViews()
            view?.findViewById<LinearLayout>(R.id.searchResultsHolder)?.visibility = View.GONE

            for (i in 0 until searchResults.length()) {
                val movieData = searchResults.getJSONObject(i)
                val backdropPath = movieData.getString("poster_path")

                val movieDataFragment = SearchResult.newInstance(
                    movieData.getString("title"),
                    movieData.getString("overview"),
                    movieData.getDouble("vote_average"),
                    movieData.getInt("id"),
                    IMG_BASE_URL + backdropPath
                )

                val fragmentManager = childFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.add(R.id.searchResultsHolder, movieDataFragment)

                // Commit the transaction
                transaction.commit()
            }

            view?.findViewById<LinearLayout>(R.id.searchResultsHolder)?.visibility = View.VISIBLE
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val timer = Timer()

        val root = inflater.inflate(R.layout.fragment_search, container, false);


        // search functionality
        val searchInput = root.findViewById<EditText>(R.id.searchBar)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!((activity as MainActivity).isOnline())) {
                    Toast.makeText(
                        activity,
                        "You are currently offline and you have no access to the internet. Please check your connection.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    val searchTerm = s.toString()
                    searchInProg?.cancel()
                    searchInProg = object : TimerTask() {
                        override fun run() {
                            search(searchTerm)
                        }
                    }

                    timer.schedule(searchInProg, 2000)
                }
            }
        })

        activity?.runOnUiThread {
//            view?.findViewById<FlexboxLayout>(R.id.filtersHolder)?.removeAllViews()
            for (i in 0 until filterButtons.size) {
                val fragmentManager = childFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.remove(filterButtons[i])
                transaction.commit()
            }
        }
        filterButtons.clear()

        for (i in filters.indices) {
            Log.i("FILTER", "ADDING " + filters[i])
            val filterBtn = FilterButton.newInstance(filters[i])
            // when clicked, it should change background colors, and add itself to the selectedFilters list

            val fragmentManager = childFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.filtersHolder, filterBtn)
            filterButtons.add(filterBtn)
            // Commit the transaction
            transaction.commit()
        }

        // open filters functionality
        val filterButton = root.findViewById<RelativeLayout>(R.id.filterHolder)
        filterButton.setOnClickListener {
            toggleFilters()
        }

        return root;
    }

    override fun onStart() {
        super.onStart()
        Log.i("Filter", "Starting")

        for (i in filterButtons.indices) {
            val btn = filterButtons[i]
            val addToList = fun(): Unit {
                // check if in selectedFilters
                if (selectedFilters.contains(filters[i])) {
                    // if is -> remove
                    selectedFilters.remove(filters[i])
                } else {
                    // if not -> add
                    selectedFilters.add(filters[i])
                }

                Log.i("FILTER", filters[i])
                // display search result
                val filteredSearchResults = arrayListOf<JSONObject>();
                if (lastSearch != null) {
                    for (j in 0 until lastSearch!!.length()) {
                        val currSearchResult = lastSearch!!.getJSONObject(j)
                        var shouldBeIncluded = true

                        Log.i("FILTER", currSearchResult.toString())
                        Log.i("FILTER", selectedFilters.toString())
                        for (k in 0 until selectedFilters.size) {
                            when (selectedFilters[k]) {
                                "Popular" -> shouldBeIncluded = (currSearchResult.getDouble("popularity")) > 100
                                "Niche" -> shouldBeIncluded = (currSearchResult.getDouble("popularity")) < 150
                                "Highly Rated" -> shouldBeIncluded = (currSearchResult.getDouble("vote_average") >= 5)
                                "Foreign" -> shouldBeIncluded = (currSearchResult.getString("original_language") != "en")
                            }

                            Log.i("FILTER", "EXAMINE FILTER: " + selectedFilters[k])

                            if (!shouldBeIncluded) {
                                break;
                            }
                        }

                        if (shouldBeIncluded) {
                            filteredSearchResults.add(currSearchResult)
                        }
                    }
                }


                displaySearchResults(JSONArray(filteredSearchResults))
            }
            btn.onClick(addToList)
        }
    }

    private fun toggleFilters() {
        activity?.runOnUiThread {
            val filtersHolder = view?.findViewById<FlexboxLayout>(R.id.filtersHolder)
            if (tasksShown) {
                // hide filters
                filtersHolder?.visibility = View.GONE
            } else {
                // show filters
                filtersHolder?.visibility = View.VISIBLE
            }
        }

        tasksShown = !tasksShown
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}