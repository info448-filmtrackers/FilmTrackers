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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.widget.LinearLayout
import edu.uw.ischool.mwoode.filmtrackers.UserMovieData


private const val TAG = "MovieHistoryFragment"


data class UserMovieData(
    val dateWatched: String,
    val liked: Boolean,
    val movieId: Int,
    val review: String
)


class MovieHistoryFragment : Fragment() {


    private var movieIdParam: Int? = null
    private lateinit var titleTextView: TextView
    private lateinit var ratingTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var imagePoster: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if movieIdParam is present in the arguments
        movieIdParam = arguments?.getInt(MOVIE_ID_PARAM)

        if (movieIdParam == null) {
            val filePath = requireActivity().filesDir.path + "/user_movie_data.json"
            readUserMovieData(filePath)?.let { userMovieDataList ->
                // Set movieIdParam to the movieId from the first object in the array
                movieIdParam = userMovieDataList.firstOrNull()?.movieId

                // If movieIdParam is still null, set it to a default value (e.g., 221)
                movieIdParam = movieIdParam ?: 388
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movie_history, container, false)

        // Clear existing views
//        view?.findViewById<LinearLayout>(R.id.movieListHolder)?.removeAllViews()

        // display all movies
        readUserMovieData(requireActivity().filesDir.path + "/user_movie_data.json")?.let { userMovieDataList ->
            for (movieInfo in userMovieDataList) {
                updateMovie(movieInfo)
            }
        }
        return view
    }

    

    private fun updateMovie(movieInfo: UserMovieData) {
        val movieId = movieInfo.movieId
        view?.findViewById<LinearLayout>(R.id.movieListHolder)?.removeAllViews()

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


//                 get img from api
//                val movieImgRequest = Request.Builder()
//                    .url("$IMG_BASE_URL/${movieData["poster_path"]}")
//                    .get()
//                    .addHeader("accept", "application/json")
//                    .addHeader("Authorization", "Bearer $BEARER_TOKEN")
//                    .build()
//
//
//                val movieImgResponse = client.newCall(movieImgRequest).execute()
//                val bitmap =
//                    BitmapFactory.decodeStream(movieImgResponse.body()?.source()?.inputStream())


                // fetch data
                val backdropPath = movieData.getString("poster_path")


                // Display movie information in UI
                activity?.runOnUiThread {
//                    view?.findViewById<LinearLayout>(R.id.movieListHolder)?.removeAllViews()
//                    view?.findViewById<LinearLayout>(R.id.movieListHolder)?.visibility = View.GONE


                    val movieListHolder = view?.findViewById<LinearLayout>(R.id.movieListHolder)

                    if (movieListHolder != null) {
                        for (i in 0 until movieListHolder.childCount) {
                            val movie = childFragmentManager.findFragmentByTag("search_result_$i")
                            if (movie != null) {
                                childFragmentManager.beginTransaction().remove(movie).commit()
                            }
                        }
                    }

                    

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
//                    val movieListHolder = view?.findViewById<LinearLayout>(R.id.movieListHolder)


                    if (!fragmentManager.isStateSaved) {
                        transaction.add(R.id.movieListHolder, historyFragment)

                        // Commit the transaction only if the state is not saved
                        transaction.commit()
                    } else {
                        Log.w(TAG, "Fragment state is saved. Fragment transaction not committed.")
                    }

//                    view?.findViewById<LinearLayout>(R.id.movieListHolder)?.visibility = View.VISIBLE
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


