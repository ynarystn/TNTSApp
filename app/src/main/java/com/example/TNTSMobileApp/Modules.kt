package com.example.TNTSMobileApp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Modules : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var circleButton: ConstraintLayout
    private lateinit var profilePicture: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_modules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        circleButton = view.findViewById(R.id.circleButton)
        profilePicture = view.findViewById(R.id.profilePicture)

        val circleButton: View = view.findViewById(R.id.circleButton)

        circleButton.setOnClickListener {
            // Create and show the dialog fragment
            val dialogFragment = LogoutDialogFragment.newInstance()
            dialogFragment.setAnchorView(circleButton) // Set the anchor view
            dialogFragment.show(childFragmentManager, "StudentDialog")
        }

        // ViewPager
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)

        viewPager.adapter = VPAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Grade 8"
                1 -> tab.text = "Grade 9"
                2 -> tab.text = "Grade 10"
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.view.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.popup_bg)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.view.background = null // Remove background if needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        // Display user's profile information in the button
        displayUserProfile()
    }


    private fun displayUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userProfilePictureUrl = currentUser.photoUrl

            // Load the user's profile picture into the button
            if (userProfilePictureUrl != null) {
                Glide.with(requireContext())
                    .load(userProfilePictureUrl)
                    .circleCrop()
                    .into(profilePicture)

                // Add a click listener to the profile picture
                profilePicture.setOnClickListener {
                    showLogoutDialog()
                }
            }
        }
    }

    private fun showLogoutDialog() {
        // Get the current user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Create an instance of the LogoutDialogFragment
            val logoutDialog = LogoutDialogFragment.newInstance()
            logoutDialog.setAnchorView(profilePicture) // Set the anchor view
            logoutDialog.setUserProfilePictureUrl(currentUser.photoUrl.toString()) // Pass the user profile picture URL
            logoutDialog.setUsername("Hello, ${currentUser.displayName ?: "Unknown User"}!")
            logoutDialog.show(childFragmentManager, "LogoutDialogFragment")
        } else {
            // add else here
        }
    }
}
