package edu.uw.ischool.mwoode.filmtrackers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    private val homePageFragment = HomePageFragment();
    private val searchFragment = SearchFragment();
    private val userHistoryFragment = MovieHistoryFragment();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setFragment(homePageFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener {
            switchToFragment(it.itemId)
            true
        }

        setupUserDataFile()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        unselectNavbarItems()
    }

    fun switchToFragment(itemId: Int) {
        val targetFragment = when (itemId) {
            R.id.search -> searchFragment
            R.id.userHistory -> userHistoryFragment
            else -> homePageFragment
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, targetFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment);
            commit();
        }
    }

    // for use later, when we are moving from a fragment on navbar to a fragment not available on navbar
    // e.g. add movie page, movie details page
    private fun unselectNavbarItems() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNavigationView != null) {
            bottomNavigationView.selectedItemId = R.id.unselectedNav
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