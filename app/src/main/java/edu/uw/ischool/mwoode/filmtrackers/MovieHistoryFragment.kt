package edu.uw.ischool.mwoode.filmtrackers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import android.graphics.Bitmap
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import android.annotation.SuppressLint


private const val TAG = "MovieHistoryFragment"

class MovieHistoryFragment : Fragment() {

    private var movieIdParam: Int? = null
    private lateinit var titleTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var imagePoster: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arguments?.let {
                movieIdParam = it.getInt(MOVIE_ID_PARAM)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_history, container, false)

        titleTextView = view.findViewById(R.id.movieTitle) as TextView

        ratingTextView = view.findViewById(R.id.movieRating) as TextView

        descriptionTextView = view.findViewById(R.id.movieDescription) as TextView

        imagePoster = view.findViewById(R.id.movieImg) as ImageView

        Log.i(TAG, "movie id param: $movieIdParam")


        // Gets movie id from add movie page
        Log.i(TAG, "movie id param: $movieIdParam")
//        val movieId = movieIdParam as? Int ?: 0 // Provide a default value, like 0, or handle it based on your use case
//        updateMovie(movieId)


        return view
    }


    private fun updateMovie() {
        if (!isOnline()) {
            Toast.makeText(
                activity,
                "You are currently offline and you have no access to the internet. Please check your connection.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val executor: Executor = Executors.newSingleThreadExecutor()
            var movieData: JSONObject


            // get movie data from api
            executor.execute {
                val client = OkHttpClient()
                val movieDataRequest = Request.Builder()
                    .url(getString(R.string.movie_details_url, movieIdParam))
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                    .build()

                val movieDataResponse = client.newCall(movieDataRequest).execute()
                movieData = JSONObject(movieDataResponse.body()?.string().toString())
                Log.i(TAG, "response: $movieData")


                // get img from api
                val movieImgRequest = Request.Builder()
                    .url("$IMG_BASE_URL/${movieData["poster_path"]}")
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                    .build()

                val movieImgResponse = client.newCall(movieImgRequest).execute()
                val bitmap =
                    BitmapFactory.decodeStream(movieImgResponse.body()?.source()?.inputStream())


                // fetch data
                val rating = (movieData["vote_average"] as Double).toInt()
//                val title = movieData["title"].toString()
//                val description = movieData["tagline"].toString()

                activity?.runOnUiThread {
                    imagePoster.setImageBitmap(bitmap)
                    titleTextView.text = movieData["title"].toString()
                    ratingTextView.text = "Rating: $rating/10"
                }
            }
        }
    }


    private fun ensureUserDataFileExists(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            val fileWriter = FileWriter(filePath)
            fileWriter.write("[]")
            fileWriter.close()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }


    companion object {
        @JvmStatic
        fun newInstance(movieId: Int) = MovieHistoryFragment().apply {
            arguments = Bundle().apply {
                putInt(MOVIE_ID_PARAM, movieId)
            }
        }
    }
}

