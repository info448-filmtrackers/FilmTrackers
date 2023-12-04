package edu.uw.ischool.mwoode.filmtrackers

import android.app.DatePickerDialog
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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Calendar
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TAG = "AddMovieFragment"

class AddMovieFragment : Fragment() {
    private var movieIdParam = -1

    private lateinit var movieCardFragment: MovieCardFragment

    private lateinit var thumbsUpToggle: ToggleButton
    private lateinit var thumbsDownToggle: ToggleButton
    private lateinit var dateWatchedEditText: EditText
    private lateinit var userReviewEditText: EditText
    private lateinit var addMovieBtn: Button

    private var userLikedMovie = ""
    private var dateWatched = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // TODO: movieId will be passed in from the search page...
            // alternatively, the search page can just pass the movie data in an intent extra to avoid doing another API call
            movieIdParam = it.getInt(MOVIE_ID_PARAM)
        }
        movieIdParam = 201
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_movie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) {
            val newMovieCardFragment = MovieCardFragment()
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, newMovieCardFragment)
                .addToBackStack(null)
                .hide(newMovieCardFragment)
                .commit()
        }

        thumbsUpToggle = view.findViewById(R.id.thumbsUpToggle) as ToggleButton
        thumbsDownToggle = view.findViewById(R.id.thumbsDownToggle) as ToggleButton
        dateWatchedEditText = view.findViewById(R.id.dateWatchedEditText) as EditText
        userReviewEditText = view.findViewById(R.id.userReviewEditText) as EditText
        addMovieBtn = view.findViewById(R.id.addMovieBtn) as Button

        updateMovieCard()

        // setup interactive components
        thumbsUpToggle.setOnClickListener {
            userLikedMovie = "true"

            thumbsUpToggle.setCompoundDrawablesWithIntrinsicBounds(null,
                context?.resources?.getDrawable(R.drawable.like_selected), null, null)
            thumbsDownToggle.setCompoundDrawablesWithIntrinsicBounds(null,
                context?.resources?.getDrawable(R.drawable.dislike_unselected), null, null)
            thumbsDownToggle.isChecked = false
        }

        thumbsDownToggle.setOnClickListener {
            userLikedMovie = "false"

            thumbsDownToggle.setCompoundDrawablesWithIntrinsicBounds(null,
                context?.resources?.getDrawable(R.drawable.dislike_selected), null, null)
            thumbsUpToggle.setCompoundDrawablesWithIntrinsicBounds(null,
                context?.resources?.getDrawable(R.drawable.like_unselected), null, null)
            thumbsUpToggle.isChecked = false
        }

        dateWatchedEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context as Context,
                { _, year, month, day ->
                    dateWatched = "$month/$day/$year"
                    dateWatchedEditText.setText(dateWatched)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        addMovieBtn.setOnClickListener {
            if (userLikedMovie.isEmpty() || dateWatched.isEmpty() || userReviewEditText.text.isNullOrEmpty()) {
                Toast.makeText(activity, "Make sure all required fields are filled.", Toast.LENGTH_LONG).show()
            } else {
                saveUserReview()

                val movieHistoryFragment = MovieHistoryFragment()
                navigateToFragment(movieHistoryFragment)
            }
        }
    }

    private fun updateMovieCard() {
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
                    .url(getString(R.string.movie_details_url, movieIdParam))
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
                    if (isAdded) {
                        movieCardFragment =
                            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as MovieCardFragment
                        movieCardFragment?.let {
                            val movieCardTitleTextView =
                                it.view?.findViewById(R.id.movieTitle) as TextView
                            val movieCardRatingTextView =
                                it.view?.findViewById(R.id.movieRating) as TextView
                            val movieCardDescTextView =
                                it.view?.findViewById(R.id.movieDescription) as TextView
                            val movieCardImageView =
                                it.view?.findViewById(R.id.movieImg) as ImageView
                            val readMoreBtn = it.view?.findViewById(R.id.readMoreBtn) as Button

                            movieCardTitleTextView.text = movieData["title"].toString()
                            movieCardRatingTextView.text = "Rating: $rating/10"
                            movieCardDescTextView.text = movieData["tagline"].toString()
                            movieCardImageView.setImageBitmap(bitmap)
                            readMoreBtn.setOnClickListener {
                                val detailedFragment = DetailedFragment.newInstance(movieIdParam)
                                navigateToFragment(detailedFragment)
                            }
                        }

                        childFragmentManager.beginTransaction()
                            .show(movieCardFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }
    }

    private fun saveUserReview() {
        val reviewJson = UserReview(movieIdParam, userLikedMovie.toBoolean(), dateWatched, userReviewEditText.text.toString())

        val filePath = "${context?.filesDir?.absolutePath}/$USER_DATA_FILE"
        Log.i(TAG, "filePath: $filePath")
        ensureUserDataFileExists(filePath)

        var userReviews: MutableList<UserReview>
        FileReader(filePath).use {
            val text = it.readText()
            userReviews = Gson().fromJson(text, object : TypeToken<MutableList<UserReview>>() {}.type)
            userReviews.add(reviewJson)
        }

        val userReviewsJson = Gson().toJson(userReviews)
        val fileWriter = FileWriter(filePath)
        fileWriter.write(userReviewsJson)
        fileWriter.close()

        resetFields()
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

    private fun resetFields() {
        thumbsUpToggle.isChecked = false
        thumbsDownToggle.isChecked = false
        thumbsUpToggle.setCompoundDrawablesWithIntrinsicBounds(null,
            context?.resources?.getDrawable(R.drawable.like_unselected), null, null)
        thumbsDownToggle.setCompoundDrawablesWithIntrinsicBounds(null,
            context?.resources?.getDrawable(R.drawable.dislike_unselected), null, null)

        dateWatchedEditText.setText("")

        userReviewEditText.setText("")
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
            commit()
        }
    }

    companion object {
        // Creates a new instance of the AddMovieFragment using the provided parameters.
        @JvmStatic
        fun newInstance(movieId: Int) =
            HomePageFragment().apply {
                arguments = Bundle().apply {
                    putInt(MOVIE_ID_PARAM, movieId)
                }
            }
    }
}