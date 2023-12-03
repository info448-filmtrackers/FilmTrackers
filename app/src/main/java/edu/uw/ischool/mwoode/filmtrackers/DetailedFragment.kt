package edu.uw.ischool.mwoode.filmtrackers

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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val TAG = "DetailedMovieFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_detailed, container, false)

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

        val movieId = 200
        updateMovie(movieId)

        return view

    }

    private fun updateMovie(movieId: Int) {
        //val detailUrl = "https://api.themoviedb.org/3/movie/162?language=en-US"
        //val creditsUrl = "https://api.themoviedb.org/3/movie/162/credits?language=en-US"

        val executor: Executor = Executors.newSingleThreadExecutor()

        val movieExecutor: Executor = Executors.newSingleThreadExecutor()
        val creditsExecutor: Executor = Executors.newSingleThreadExecutor()
        var movieData: JSONObject
        var movieCreditsData: JSONObject

        //executor.execute {
        movieExecutor.execute {
            val client = OkHttpClient()
            val movieDataRequest = Request.Builder()
                .url(getString(R.string.movie_details_url, movieId)) // just replace 2nd number with movie id
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                .build()

            val movieDataResponse = client.newCall(movieDataRequest).execute()
            movieData = JSONObject(movieDataResponse.body()?.string())
            Log.i(TAG, "response: $movieData")

            val movieCreditsRequest = Request.Builder()
                .url(getString(R.string.movie_credits_url, movieId)) // just replace 2nd number with movie id
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer $BEARER_TOKEN")
                .build()

            val movieCreditsResponse = client.newCall(movieCreditsRequest).execute()
            movieCreditsData = JSONObject(movieCreditsResponse.body()?.string())
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
            //val crewList = arrayListOf<String>()

            val directorList = arrayListOf<String>()
            val writerList = arrayListOf<String>()
            val producerList = arrayListOf<String>()
            val editorList = arrayListOf<String>()

            /*for (i in 0 until genre.length()) {
                genreList.add(genre[i].toString())
            }*/

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

            /*for (i in 0 until cast.length()) {
                val castMember = cast.getJSONObject(i)
                val actorName = castMember.getString("name")
                castList.add(actorName)
                //castList.add(cast[i].toString())
            }*/

            for (i in 0 until 15) {
                val castMember = cast.getJSONObject(i)
                val actorName = castMember.getString("name")
                castList.add(actorName)
                //castList.add(cast[i].toString())
            }

            for (i in 0 until crew.length()) {
                val crewMember = crew.getJSONObject(i)

                //val crewJob = crewMember.getString("job")

                /*
                val directorList = arrayListOf<String>()
                val writerList = arrayListOf<String>()
                val producerList = arrayListOf<String>()
                val editorList = arrayListOf<String>()
                 */

                // when(crewJob) {
                when(crewMember.getString("job")) {
                    "Director" -> directorList.add(crewMember.getString("name"))
                    "Screenplay" -> writerList.add(crewMember.getString("name"))
                    "Producer" -> producerList.add(crewMember.getString("name"))
                    "Editor" -> editorList.add(crewMember.getString("name"))
                }

                /*
                val director = crewMember.getString("job") == "Director"
                val writer = crewMember.getString("job") == "Writer"
                val producer = crewMember.getString("job") == "Producer"
                val editor = crewMember.getString("job") == "Editor"
                */

                //crewList.add(director)

                //crewList.add(cast[i].toString())
            }

            activity?.runOnUiThread {
                movieImage.setImageBitmap(bitmap)
                movieTitle.text = movieData["title"].toString()
                movieRating.text = "Rating: $rating/10"
                movieDate.text = "Release Date: " + movieData["release_date"].toString()
                movieGenre.text = "Genre: $genreList"
                movieCompany.text = "Prod. Company: $companyList"
                movieDirector.text = "Director: $directorList"
                movieWriter.text = "Writer: $writerList"
                movieProducer.text = "Producer: $producerList"
                movieEditor.text = "Editor: $editorList"
                movieActors.text = "Actors: $castList"
                movieSummary.text = "Summary:\n" + movieData["overview"].toString()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}