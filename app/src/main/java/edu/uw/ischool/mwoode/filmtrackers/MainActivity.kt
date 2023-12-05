package edu.uw.ischool.mwoode.filmtrackers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private val homePageFragment = HomePageFragment();
    private val searchFragment = SearchFragment();
    private val userHistoryFragment = MovieHistoryFragment();
    private val addMovieFragment = AddMovieFragment(); // TODO: Remove later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFragment(homePageFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setFragment(homePageFragment)
                R.id.search -> setFragment(searchFragment)
                R.id.userHistory -> setFragment(userHistoryFragment)
                R.id.addMovie -> setFragment(addMovieFragment) // TODO: Remove later
            }
            true
        }

        setupUserDataFile()
    }

    fun switchToFragment(fragmentId: Int) {
        val targetFragment = when (fragmentId) {
            R.id.search -> searchFragment
            R.id.userHistory -> userHistoryFragment
            R.id.addMovie -> addMovieFragment // TODO: remove later
            else -> homePageFragment
        }
        setFragment(targetFragment)
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