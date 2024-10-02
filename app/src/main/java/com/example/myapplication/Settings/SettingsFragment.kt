package com.example.myapplication.Settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.LoginRegister.LoginActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var buttonChangeEmail: FrameLayout
    private lateinit var buttonChangePhone: FrameLayout
    private lateinit var buttonDeleteAccount: FrameLayout
    private lateinit var buttonLogout: FrameLayout

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        buttonChangeEmail = view.findViewById(R.id.frameLayoutChangeEmail)
        buttonChangePhone = view.findViewById(R.id.frameLayoutChangePhone)
        buttonDeleteAccount = view.findViewById(R.id.frameLayoutDeleteAccount)
        buttonLogout = view.findViewById(R.id.frameLayoutLogout)

        auth = FirebaseAuth.getInstance()

        // Set text programmatically (optional if already set in XML)
        setButtonText(buttonChangeEmail, getString(R.string.ChangeEmail))
        setButtonText(buttonChangePhone, getString(R.string.ChangePhoneNumber))
        setButtonText(buttonDeleteAccount, getString(R.string.DeleteAccount))
        setButtonText(buttonLogout, getString(R.string.LogOut))

        buttonChangeEmail.setOnClickListener {
            navigateToFragment(ChangeEmailFragment())
        }

        buttonChangePhone.setOnClickListener {
            navigateToFragment(ChangePhoneFragment())
        }

        buttonDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
    }

    private fun setButtonText(buttonLayout: FrameLayout, text: String) {
        val textView: TextView = buttonLayout.findViewById(R.id.buttonText)
        textView.text = text
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.LoggingOut))
            .setMessage(getString(R.string.LoggingOutConfirm))
            .setPositiveButton(getString(R.string.LogoutYes)) { _, _ ->
                auth.signOut()
                Toast.makeText(context, getString(R.string.LoggedOut), Toast.LENGTH_SHORT).show()
                val intent = Intent(context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton(getString(R.string.LogoutNo), null)
            .create()

        dialog.show()
    }

    private fun showDeleteAccountConfirmationDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.DeleteAccountTitle))
            .setMessage(getString(R.string.DeleteAccountConfirmQuestion))
            .setPositiveButton(R.string.LogoutYes) { _, _ ->
                val user = auth.currentUser
                user?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context,
                                getString(R.string.DeletedAccount), Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            activity?.finish()
                        } else {
                            Toast.makeText(context,
                                getString(R.string.DeletedAccountError, task.exception?.message), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton(R.string.LogoutNo, null)
            .create()

        dialog.show()
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
