package edu.uw.ischool.mwoode.filmtrackers


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import android.widget.LinearLayout


private const val TAG = "MovieHistoryFragment"


data class UserMovieData(
    val dateWatched: String,
    val liked: Boolean,
    val movieId: Int,
    val review: String
)


class MovieHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_history, container, false)
    }

    override fun onResume() {
        super.onResume()

        updatePage()
    }

    private fun updatePage() {
        val movieListHolder = view?.findViewById<LinearLayout>(R.id.movieListHolder)
        if (movieListHolder != null) {
            Log.i(TAG, "movie list holder: $movieListHolder, with children count: ${movieListHolder.childCount}")
            for (i in 0 until movieListHolder.childCount) {
                val movie = childFragmentManager.findFragmentByTag("movie_list_$i")
                Log.i(TAG, "current movie: $movie")
                if (movie != null) {
                    Log.i(TAG, "removing movie $movie")
                    childFragmentManager.beginTransaction().remove(movie).commit()
                }
            }
        }

        // display all movies
        readUserMovieData(requireActivity().filesDir.path + "/user_movie_data.json")?.let { userMovieDataList ->
            Log.i(TAG, "user movie data: $userMovieDataList")
            for (i in userMovieDataList.indices) {
                updateMovie(userMovieDataList[i], i)
            }
        }
    }


    private fun updateMovie(movieInfo: UserMovieData, currMovie: Int) {
        val movieId = movieInfo.movieId

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
                    .url(getString(R.string.movie_details_url, movieId))
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                    .build()


                val movieDataResponse = client.newCall(movieDataRequest).execute()
                movieData = JSONObject(movieDataResponse.body()?.string().toString())
                Log.i(TAG, "response: $movieData")


                // fetch data
                val backdropPath = movieData.getString("poster_path")


                // Display movie information in UI
                activity?.runOnUiThread {
                    val historyFragment = MovieList.newInstance(
                        movieData.getString("title"),
                        movieData.getString("overview"),
                        movieData.getDouble("vote_average"),
                        movieData.getInt("id"),
                        IMG_BASE_URL + backdropPath,
                        movieInfo.review,
                        movieInfo.liked,
                        movieInfo.dateWatched
                    )
                    val fragmentManager = childFragmentManager
                    val transaction = fragmentManager.beginTransaction()

                    if (isAdded) {
                        Log.i(TAG, "adding movie: $historyFragment with tag ${"movie_list_$currMovie"}")
                        transaction.add(R.id.movieListHolder, historyFragment, "movie_list_$currMovie")
                        transaction.commit()
                    }
                }
            }
        }
    }



    private fun readUserMovieData(filePath: String): List<UserMovieData>? {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val fileReader = FileReader(file)
                val gson = Gson()

                // Read the JSON array from the file
                val arrayType = object : TypeToken<List<UserMovieData>>() {}.type
                val userMovieDataList: List<UserMovieData> = gson.fromJson(fileReader, arrayType)


                // Check if the array is not empty
                if (userMovieDataList.isNotEmpty()) {
                    return userMovieDataList
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading user_movie_data.json", e)
        }

        return null
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