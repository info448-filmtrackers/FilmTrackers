package edu.uw.ischool.mwoode.filmtrackers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val TITLE = "originalTitle"
private const val DESC = "description"
private const val RATING = "rating"
private const val IMG = "imgUrl"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchResult.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchResult : Fragment() {
    // TODO: Rename and change types of parameters
    private var originalTitle: String? = null
    private var description: String? = null
    private var rating: Double? = null
    private var imgUrl: String? = null
    private var movieIdParam: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            originalTitle = it.getString(TITLE)
            description = it.getString(DESC)
            rating = it.getDouble(RATING)
            imgUrl = it.getString(IMG)
            movieIdParam = it.getInt(MOVIE_ID_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_result, container, false)

        val titleTextView = view.findViewById<TextView>(R.id.movieTitle)
        titleTextView.text = originalTitle

        val ratingTextView = view.findViewById<TextView>(R.id.movieRating)
        ratingTextView.text = "$rating/10"

        val descriptionTextView = view.findViewById<TextView>(R.id.movieDescription)
        descriptionTextView.text = description

        // Moves to add movie page
        val addMovieButton = view.findViewById<ImageView>(R.id.addButton)
        addMovieButton.setOnClickListener {
            val addMovieFragment = AddMovieFragment.newInstance(movieIdParam)
            navigateToFragment(addMovieFragment)
        }

        if (imgUrl != null) {
            val executor: Executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    val client = OkHttpClient()
                    val movieImgRequest = Request.Builder()
                        .url(imgUrl)
                        .get()
                        .addHeader("accept", "application/json")
                        .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                        .build()

                    val movieImgResponse = client.newCall(movieImgRequest).execute()
                    val bitmap = BitmapFactory.decodeStream(movieImgResponse.body()?.source()?.inputStream())

                    // fetch the img to display
                    val imagePoster = view.findViewById<ImageView>(R.id.movieImg)
                    activity?.runOnUiThread {
                        imagePoster.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
            commit()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchResult.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            title: String,
            description: String,
            rating: Double,
            id: Int,
            imgUrl: String) =
            SearchResult().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(DESC, description)
                    putDouble(RATING, rating)
                    putInt(MOVIE_ID_PARAM, id)
                    putString(IMG, imgUrl)
                }
            }
    }
}