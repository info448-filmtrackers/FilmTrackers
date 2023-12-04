package edu.uw.ischool.mwoode.filmtrackers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            displaySearchResults(JSONArray(check.toString()))
        }
    }

    fun displaySearchResults(searchResults: JSONArray) {
        Log.i("SEARCH", searchResults.toString())

        activity?.runOnUiThread {
            view?.findViewById<LinearLayout>(R.id.searchResultsHolder)?.removeAllViews()
        }

        for (i in 0 until searchResults.length()) {
            val movieData = searchResults.getJSONObject(i)
            val backdropPath = movieData.getString("poster_path")

            val movieDataFragment = SearchResult.newInstance(
                movieData.getString("title"),
                movieData.getString("overview"),
                movieData.getDouble("vote_average"),
                IMG_BASE_URL + backdropPath
            )

            val fragmentManager = childFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.searchResultsHolder, movieDataFragment)

            // Commit the transaction
            transaction.commit()
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
                val searchTerm = s.toString()
                searchInProg?.cancel()
                searchInProg = object : TimerTask() {
                    override fun run() {
                        search(searchTerm)
                    }
                }

                timer.schedule(searchInProg, 2000)
            }
        })

        // open filters functionality
        val filterButton = root.findViewById<RelativeLayout>(R.id.filterHolder)
        filterButton.setOnClickListener {
            toggleFilters()
        }

        return root;
    }

    private fun toggleFilters() {

        activity?.runOnUiThread {
            val buttonText = view?.findViewById<TextView>(R.id.filtersText)
            if (tasksShown) {
                buttonText?.setText("Filters ▼")
            } else {
                buttonText?.setText("Filters ►")
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