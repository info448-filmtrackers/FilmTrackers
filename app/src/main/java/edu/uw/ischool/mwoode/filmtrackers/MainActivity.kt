package edu.uw.ischool.mwoode.filmtrackers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.uw.ischool.mwoode.filmtrackers.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_main)
        setContentView(binding.root)
    }
}