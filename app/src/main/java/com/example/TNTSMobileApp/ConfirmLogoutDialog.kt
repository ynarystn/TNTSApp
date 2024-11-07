package com.example.TNTSMobileApp

import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ConfirmLogoutDialog : DialogFragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = activity?.layoutInflater
        val dialogView = inflater?.inflate(R.layout.dialog_confirm_logout, null)

        val confirmDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        confirmDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Set transparent background

        val displayMetrics = requireContext().resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.85).toInt()

        confirmDialog.setOnShowListener {
            val dialogWindow = confirmDialog.window
            dialogWindow?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        dialogView?.findViewById<Button>(R.id.buttonYes)?.setOnClickListener {
            // Handle the logout action
            signOut()
            val intent = Intent(requireContext(), LoadingScreen::class.java)
            startActivity(intent)

            // Dismiss the confirmation dialog and the logout dialog
            confirmDialog.dismiss()
            (parentFragment as LogoutDialogFragment).dismiss()
        }

        dialogView?.findViewById<Button>(R.id.buttonNo)?.setOnClickListener {
            confirmDialog.dismiss() // Just close the confirmation dialog
        }

        return confirmDialog
    }

    companion object {
        fun newInstance(): ConfirmLogoutDialog {
            return ConfirmLogoutDialog()
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