package com.example.TNTSMobileApp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.example.TNTSMobileApp.databinding.ActivityMainBinding
import android.content.SharedPreferences
import android.view.View
import android.widget.VideoView

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences // Initialize SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Home())

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)

        // Check if user is already logged in
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false) // Get the isLoggedIn flag from SharedPreferences

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.home -> replaceFragment(Home())
                R.id.modules -> replaceFragment(Modules())
                R.id.tutorials -> replaceFragment(Tutorials())
                R.id.challenges -> replaceFragment(Challenges())
                R.id.settings -> replaceFragment(Settings())
                else -> {
                }
            }
            true
        }
        if (!isLoggedIn) { // Only show the welcome message dialog if user is not logged in
            showWelcomeMessageDialog()
        }
    }

    fun replaceFragment(fragment : Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        Log.d("MainActivity", "Replacing fragment: ${fragment.javaClass.simpleName}")
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    private fun showWelcomeMessageDialog() {
        val welcomeDialogView = layoutInflater.inflate(R.layout.dialog_motivational_message, null)
        val welcomeDialog = AlertDialog.Builder(this)
            .setView(welcomeDialogView)
            .create()

        // Set dialog background to transparent
        welcomeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val playVideoButton = welcomeDialogView.findViewById<Button>(R.id.btn_play_video)

        playVideoButton.setOnClickListener {
            // Dismiss the welcome dialog
            welcomeDialog.dismiss()

            // Show the video dialog
            showVideoDialog()

            // Mark the user as logged in
            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
        }

        welcomeDialog.show()
    }

    private fun showVideoDialog() {
        // Create a new dialog for playing the video
        val videoDialog = AlertDialog.Builder(this)
            .create()

        // Inflate the video view layout
        val videoViewLayout = layoutInflater.inflate(R.layout.dialog_video_view, null)
        videoDialog.setView(videoViewLayout)

        // Set video source from drawable resource
        val videoView = videoViewLayout.findViewById<VideoView>(R.id.video_view)
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.mvideo)
        videoView.setVideoURI(videoUri)

        // Start playing the video
        videoView.start()

        // Show the video dialog
        videoDialog.show()
    }
}