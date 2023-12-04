package edu.uw.ischool.mwoode.filmtrackers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileWriter

//import edu.uw.ischool.mwoode.filmtrackers.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

//    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val homePageFragment = HomePageFragment();
        val searchFragment = SearchFragment();
        val userHistoryFragment = MovieHistoryFragment();
        val addMovieFragment = AddMovieFragment();

        setFragment(homePageFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setFragment(homePageFragment)
                R.id.search -> setFragment(searchFragment)
                R.id.userHistory -> setFragment(userHistoryFragment)
                R.id.addMovie -> setFragment(addMovieFragment)
            }
            true
        }

        val test = supportFragmentManager.findFragmentByTag("fragment_homepage_option")
        Log.i("INFO", test.toString())

        setupUserDataFile()
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment);
            commit();
        }
    }

    private fun setupUserDataFile() {
        val filePath = "${this.filesDir?.absolutePath}/$USER_DATA_FILE"
        val file = File(filePath)
        if (!file.exists()) {
            val fileWriter = FileWriter(filePath)
            fileWriter.write("[]")
            fileWriter.close()
        }
    }
}