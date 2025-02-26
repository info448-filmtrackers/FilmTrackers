package edu.uw.ischool.mwoode.filmtrackers


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executor
import java.util.concurrent.Executors


private const val TITLE = "originalTitle"
private const val DESC = "description"
private const val RATING = "rating"
private const val IMG = "imgUrl"
private const val REVIEW = "review"
private const val LIKED = "liked"
private const val DATE = "dateWatched"


/**
 * A simple [Fragment] subclass.
 * Use the [MovieList.newInstance] factory method to
 * create an instance of this fragment.
 */
class MovieList : Fragment() {
    private var originalTitle: String? = null
    private var description: String? = null
    private var rating: Double? = null
    private var imgUrl: String? = null
    private var movieIdParam: Int = -1
    private var review: String? = null
    private var liked: Boolean? = true
    private var dateWatched: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            originalTitle = it.getString(TITLE)
            description = it.getString(DESC)
            rating = it.getDouble(RATING)
            imgUrl = it.getString(IMG)
            movieIdParam = it.getInt(MOVIE_ID_PARAM)
            review = it.getString(REVIEW)
            liked = it.getBoolean(LIKED)
            dateWatched = it.getString(DATE)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_movie_list, container, false)


        val titleTextView = view.findViewById<TextView>(R.id.movieTitle)
        titleTextView.text = originalTitle


        val dateTextView = view.findViewById<TextView>(R.id.date)
        dateTextView.text = "Date Watched: $dateWatched"


        val reviewTextView = view.findViewById<TextView>(R.id.userReview)
        reviewTextView.text = "Your Review: $review"


        val likedButton = view.findViewById<ImageView>(R.id.likeButton)
       if (liked == true){
           likedButton.setImageResource(R.drawable.like_unselected)
       } else {
           likedButton.setImageResource(R.drawable.dislike_unselected)
       }


        if (((activity as MainActivity).isOnline())) {
            Log.i("IMG", imgUrl.toString())
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
                        val bitmap = BitmapFactory.decodeStream(
                            movieImgResponse.body()?.source()?.inputStream()
                        )

                        // fetch the img to display
                        val imagePoster = view.findViewById<ImageView>(R.id.movieImg)
                        activity?.runOnUiThread {
                            if (bitmap != null) {
                                imagePoster.setImageBitmap(bitmap)
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }



        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MovieList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(title: String,
                        description: String,
                        rating: Double,
                        id: Int,
                        imgUrl: String,
                        review: String,
                        liked: Boolean,
                        dateWatched: String) =
            MovieList().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(DESC, description)
                    putDouble(RATING, rating)
                    putInt(MOVIE_ID_PARAM, id)
                    putString(IMG, imgUrl)
                    putString(REVIEW, review)
                    putBoolean(LIKED, liked)
                    putString(DATE, dateWatched)
                }
            }
    }
}

