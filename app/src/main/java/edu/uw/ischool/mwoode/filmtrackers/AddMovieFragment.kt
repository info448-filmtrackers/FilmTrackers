package edu.uw.ischool.mwoode.filmtrackers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "AddMovieFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [HomePageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddMovieFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var movieCardImageView: ImageView
    private lateinit var movieCardTitleTextView: TextView
    private lateinit var movieCardRatingTextView: TextView
    private lateinit var movieCardDescTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun updateMovieCard(movieId: Int) {
        // TODO: will complete checks for internet connectivity later
        if (!isOnline()) {
            Toast.makeText(
                activity,
                "You are currently offline and you have no access to the internet. Please check your connection.",
                Toast.LENGTH_LONG).show()
        } else {
            val executor: Executor = Executors.newSingleThreadExecutor()
            executor.execute {
                val client = OkHttpClient()

                // get the movie data
                val movieDataRequest = Request.Builder()
                    .url(getString(R.string.movie_details_url, movieId))
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                    .build()

                val movieDataResponse = client.newCall(movieDataRequest).execute()
                val movieData = JSONObject(movieDataResponse.body()?.string())
                Log.i(TAG, "response: $movieData")

                // get the image
                val movieImgRequest = Request.Builder()
                    .url("$IMG_BASE_URL/${movieData["poster_path"]}")
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                    .build()

                val movieImgResponse = client.newCall(movieImgRequest).execute()
                val bitmap = BitmapFactory.decodeStream(movieImgResponse.body()?.source()?.inputStream())

                val rating = (movieData["vote_average"] as Double).toInt()
                activity?.runOnUiThread {
                    movieCardTitleTextView.text = movieData["title"].toString()
                    movieCardRatingTextView.text = "$rating/10"
                    movieCardDescTextView.text = movieData["tagline"].toString()
                    movieCardImageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_movie, container, false)

        movieCardImageView = view?.findViewById(R.id.movieImg) as ImageView
        movieCardTitleTextView = view.findViewById(R.id.movieTitle) as TextView
        movieCardRatingTextView = view.findViewById(R.id.movieRating) as TextView
        movieCardDescTextView = view.findViewById(R.id.movieDescription) as TextView

        // movieId will be passed in from the search page...
        // alternatively, the search page can just pass the movie data in an intent extra to avoid doing another API call
        val movieId = 199
        updateMovieCard(movieId)

        // Inflate the layout for this fragment
        return view
    }

    private fun isOnline(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.i(TAG, "MainActivity: Is this device is online? $isOnline")
        return isOnline
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomePageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomePageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}