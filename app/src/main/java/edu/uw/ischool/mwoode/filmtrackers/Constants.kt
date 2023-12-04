package edu.uw.ischool.mwoode.filmtrackers

// API CONSTANTS
const val IMG_BASE_URL = "https://image.tmdb.org/t/p/w500"
const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI2Y2JmOTRjY2RmNzM2Y2JhNmI4NjM2MGI5NTYwZjQ0MiIsInN1YiI6IjY1NjY3ZDkyNmMwYjM2MDBjNzQ1OTVmNyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3OXy8lCU6T9Fl6AIvrSEPgGVtetETGqYItUuXYKcUJE"

// ADD MOVIE CONSTANTS
const val USER_DATA_FILE = "user_movie_data.json"

// FRAGMENT PARAM CONSTANTS
const val MOVIE_ID_PARAM = "movieIdParam"

data class UserReview(var movieId: Int, var liked: Boolean, var dateWatched: String, var review: String)