package edu.uw.ischool.mwoode.filmtrackers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        if (!isOnline()) {
            Toast.makeText(
                this,
                "You are currently offline and you have no access to the internet. Please check your connection.",
                Toast.LENGTH_SHORT).show()
        }

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

    fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
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