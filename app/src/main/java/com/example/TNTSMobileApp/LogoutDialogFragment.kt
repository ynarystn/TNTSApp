package com.example.TNTSMobileApp

import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LogoutDialogFragment : DialogFragment() {

    private var anchorView: View? = null
    private var userProfilePictureUrl: String? = null
    private var username: String? = null
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
    }

    // Update the googleSignInLauncher to handle the case when no account is selected
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Handle the case where the user did not select an account
            if (e.statusCode == 12501) {
                // Attempt to sign in with the last signed-in account
                val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val savedIdToken = sharedPreferences.getString("last_signed_in_account", null)
                if (savedIdToken != null) {
                    // Automatically sign in with the saved account
                    firebaseAuthWithGoogle(savedIdToken, true)
                } else {
                    Toast.makeText(requireContext(), "No account selected and no previous account found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the dialog layout
        val inflater = activity?.layoutInflater
        val dialogView = inflater?.inflate(R.layout.logout_dialog, null)

        // Build the dialog using AlertDialog.Builder
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        // Find the views in the dialog
        val profileImageView: ImageView? = dialogView?.findViewById(R.id.imageViewProfile)
        val usernameTextView: TextView? = dialogView?.findViewById(R.id.textViewMessage)
        val switchAccountButton: Button? = dialogView?.findViewById(R.id.buttonSwitchAccount)
        val signOutButton: Button? = dialogView?.findViewById(R.id.buttonSignOut)

        // Load the user's profile picture into the image view
        userProfilePictureUrl?.let {
            Glide.with(requireContext())
                .load(it)
                .circleCrop()
                .into(profileImageView!!)
        }

        // Set the username text
        usernameTextView?.text = username

        // Set click listeners for the buttons
        switchAccountButton?.setOnClickListener {
            switchAccount() // Start the Google Sign-In process
            //dismiss() // Dismiss the logout dialog
        }

        signOutButton?.setOnClickListener {
            showConfirmLogoutDialog() // Show the confirmation dialog
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        anchorView?.let {
            dialog?.let {
                val displayMetrics = resources.displayMetrics
                val width = (displayMetrics.widthPixels * 0.90).toInt() // 90% of screen width
                it.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                it.window?.setGravity(android.view.Gravity.CENTER_HORIZONTAL)
                it.window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    fun setAnchorView(view: View) {
        this.anchorView = view
    }

    fun setUserProfilePictureUrl(url: String) {
        this.userProfilePictureUrl = url
    }

    fun setUsername(username: String) {
        this.username = username
    }

    companion object {
        // Method to create an instance of the dialog
        fun newInstance(): LogoutDialogFragment {
            return LogoutDialogFragment()
        }
    }

    private fun showConfirmLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_logout, null)

        // Create the confirmation dialog
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Set transparent background

        // Calculate the width of the dialog
        val displayMetrics = requireContext(). resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.85).toInt ()

        confirmDialog.setOnShowListener {
            val dialogWindow = confirmDialog.window
            dialogWindow?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        dialogView.findViewById<Button>(R.id.buttonYes).setOnClickListener {
            // Handle the logout action
            signOut()
            // Start the LoadingScreen activity
            val intent = Intent(requireContext(), LoadingScreen::class.java)
            startActivity(intent)

            // Dismiss the confirmation dialog and the logout dialog
            confirmDialog.dismiss()

        }

        dialogView.findViewById<Button>(R.id.buttonNo).setOnClickListener {
            confirmDialog.dismiss() // Just close the confirmation dialog
            dismiss()
        }

        confirmDialog.show()
    }

    private fun switchAccount() {
        // Get the currently signed-in account
        val currentAccount = GoogleSignIn.getLastSignedInAccount(requireActivity())

        // Save the current account information if it exists
        if (currentAccount != null) {
            val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("last_signed_in_account", currentAccount.idToken) // Save the ID token
                apply()
            }
        }

        // Sign out the current user
        auth.signOut()

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Sign out from Google Sign-In client
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            // Check if there is a previously signed-in account
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity())
            if (lastSignedInAccount != null) {
                // Automatically sign in with the last account
                firebaseAuthWithGoogle(lastSignedInAccount.idToken!!)
            } else {
                // No account found, start the sign-in process
                signIn()
            }
        }
    }

    private fun signIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent

        // Launch the sign-in intent
        googleSignInLauncher.launch(signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String, isSavedAccount: Boolean = false) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (isSavedAccount) {
                        Toast.makeText(requireContext(), "Last Account Signed In", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    }
                    // Start MainActivity and finish the current activity
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish() // Use requireActivity() to finish the activity
                } else {
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        auth.signOut()

        // Sign out from Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut()
            .addOnCompleteListener(requireActivity()) {
                val sharedPreferences = requireActivity().getSharedPreferences("user_data", MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("is_logged_in", false).apply()

                val intent = Intent(activity, LoadingScreen::class.java)
                startActivity(intent)
                activity?.finish()
            }
    }
}