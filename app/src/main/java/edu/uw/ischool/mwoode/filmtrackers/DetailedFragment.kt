package edu.uw.ischool.mwoode.filmtrackers

import android.annotation.SuppressLint
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
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors


private const val TAG = "DetailedMovieFragment"

class DetailedFragment : Fragment() {
    private var movieIdParam: Int? = null
    private var addMovieFragmentId: Int? = null

    private lateinit var backButton: ImageView

    private lateinit var movieImage: ImageView
    private lateinit var movieTitle: TextView
    private lateinit var movieRating: TextView
    private lateinit var movieDate: TextView
    private lateinit var movieGenre: TextView
    private lateinit var movieDirector: TextView
    private lateinit var movieWriter: TextView
    private lateinit var movieProducer: TextView
    private lateinit var movieEditor: TextView
    private lateinit var movieCompany: TextView
    private lateinit var movieActors: TextView
    private lateinit var movieSummary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            movieIdParam = it.getInt(MOVIE_ID_PARAM)
            addMovieFragmentId = it.getInt(ADD_MOVIE_FRAGMENT_ID_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detailed, container, false)

        backButton = view.findViewById(R.id.backArrow) as ImageView

        movieImage = view?.findViewById(R.id.moviePoster) as ImageView
        movieTitle = view.findViewById(R.id.detailTitle) as TextView
        movieRating = view.findViewById(R.id.detailRating) as TextView
        movieDate = view.findViewById(R.id.detailDate) as TextView
        movieGenre = view.findViewById(R.id.detailGenre) as TextView
        movieDirector = view.findViewById(R.id.detailDirector) as TextView
        movieWriter = view.findViewById(R.id.detailWriter) as TextView
        movieProducer = view.findViewById(R.id.detailProducer) as TextView
        movieEditor = view.findViewById(R.id.detailEditor) as TextView
        movieCompany = view.findViewById(R.id.detailCompany) as TextView
        movieActors = view.findViewById(R.id.detailActor) as TextView
        movieSummary = view.findViewById(R.id.detailSummary) as TextView

        // Gets movie id from add movie page
        Log.i(TAG, "movie id param: $movieIdParam")
        val movieId = movieIdParam as Int
        updateMovie(movieId)

        return view

    }

    @SuppressLint("SetTextI18n")
    private fun updateMovie(movieId: Int) {
        val executor: Executor = Executors.newSingleThreadExecutor()
        var movieData: JSONObject
        var movieCreditsData: JSONObject

        // Sets the detail page UI
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

            val movieCreditsRequest = Request.Builder()
                .url(getString(R.string.movie_credits_url, movieId))
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                .build()

            val movieCreditsResponse = client.newCall(movieCreditsRequest).execute()
            movieCreditsData = JSONObject(movieCreditsResponse.body()?.string().toString())
            Log.i(TAG, "response: $movieCreditsData")

            val movieImgRequest = Request.Builder()
                .url("$IMG_BASE_URL/${movieData["poster_path"]}")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                .build()

            val movieImgResponse = client.newCall(movieImgRequest).execute()
            val bitmap = BitmapFactory.decodeStream(movieImgResponse.body()?.source()?.inputStream())

            val rating = (movieData["vote_average"] as Double).toInt()
            val genre = movieData.getJSONArray("genres")
            val company = movieData.getJSONArray("production_companies")
            val cast = movieCreditsData.getJSONArray("cast")
            val crew = movieCreditsData.getJSONArray("crew")

            val genreList = arrayListOf<String>()
            val companyList = arrayListOf<String>()

            val castList = arrayListOf<String>()
            val directorList = arrayListOf<String>()
            val writerList = arrayListOf<String>()
            val producerList = arrayListOf<String>()
            val editorList = arrayListOf<String>()


            for (i in 0 until genre.length()) {
                val genres = genre.getJSONObject(i)
                val genreName = genres.getString("name")
                genreList.add(genreName)
            }

            for (i in 0 until company.length()) {
                val companies = company.getJSONObject(i)
                val companyName = companies.getString("name")
                companyList.add(companyName)
            }

            for (i in 0 until cast.length()) {
                val castMember = cast.getJSONObject(i)
                val actorName = castMember.getString("name")
                castList.add(actorName)
            }

            for (i in 0 until crew.length()) {
                val crewMember = crew.getJSONObject(i)

                when(crewMember.getString("job")) {
                    "Director" -> directorList.add(crewMember.getString("name"))
                    "Screenplay" -> writerList.add(crewMember.getString("name"))
                    "Producer" -> producerList.add(crewMember.getString("name"))
                    "Editor" -> editorList.add(crewMember.getString("name"))
                }
            }

            activity?.runOnUiThread {
                movieImage.setImageBitmap(bitmap)
                movieTitle.text = movieData["title"].toString()
                movieRating.text = "Rating: $rating/10"
                movieDate.text = "Release Date: " + movieData["release_date"].toString()
                movieGenre.text = "Genre: " + genreList.toString().replace("[", "").replace("]", "")
                movieCompany.text = "Prod. Company: " + companyList.toString().replace("[", "").replace("]", "")
                movieDirector.text = "Director: " + directorList.toString().replace("[", "").replace("]", "")
                movieWriter.text = "Writer: " + writerList.toString().replace("[", "").replace("]", "")
                movieProducer.text = "Producer: " + producerList.toString().replace("[", "").replace("]", "")
                movieEditor.text = "Editor: " + editorList.toString().replace("[", "").replace("]", "")
                movieActors.text = "Actors: " + castList.toString().replace("[", "").replace("]", "")
                movieSummary.text = "Summary:\n" + movieData["overview"].toString()

                backButton.setOnClickListener {
                    fragmentManager?.popBackStackImmediate()
//                    Log.i(TAG, "add movie fragment id: $addMovieFragmentId")
//                    val addMovieFragment = childFragmentManager.findFragmentById(addMovieFragmentId as Int) as Fragment
//                    switchFragment(addMovieFragment)
                }
            }
        }
    }

    // Returns to previous page
    private fun switchFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            addToBackStack(null)
            commit();
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(movieId: Int, addMovieFragmentId: Int) =
            DetailedFragment().apply {
                arguments = Bundle().apply {
                    putInt(MOVIE_ID_PARAM, movieId)
                    putInt(ADD_MOVIE_FRAGMENT_ID_PARAM, addMovieFragmentId)
                }
            }
    }
}