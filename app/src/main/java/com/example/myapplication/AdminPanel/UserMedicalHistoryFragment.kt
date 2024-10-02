package com.example.myapplication.AdminPanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class UserMedicalHistoryFragment : Fragment() {

    private lateinit var medicalHistoryLayout: LinearLayout
    private lateinit var addMedicalHistoryButton: Button
    private val db = FirebaseFirestore.getInstance()

    private var userId: String? = null
    private var userFullName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_medical_history, container, false)

        medicalHistoryLayout = view.findViewById(R.id.medicalHistoryLayout)
        addMedicalHistoryButton = view.findViewById(R.id.addMedicalHistoryButton)

        userId = arguments?.getString("userId")
        userFullName = arguments?.getString("userFullName")

        if (userId != null) {
            loadMedicalHistory(userId!!)
        }

        addMedicalHistoryButton.setOnClickListener {
            showAddMedicalHistoryDialog()
        }

        return view
    }

    private fun loadMedicalHistory(userId: String) {
        db.collection("medicalHistory")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                medicalHistoryLayout.removeAllViews()
                for (document in documents) {
                    val date = document.getTimestamp("date")
                    val description = document.getString("description") ?: getString(R.string.NoDescription)
                    val treatmentPlan = document.getString("treatmentPlan") ?: getString(R.string.NoTreatmentPlan)


                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val firstName = userDoc.getString("firstName") ?: "Unknown"
                            val lastName = userDoc.getString("lastName") ?: "User"
                            val fullName = "$firstName $lastName"

                            val historyView = LayoutInflater.from(context).inflate(R.layout.item_medical_history, medicalHistoryLayout, false)
                            val dateTextView = historyView.findViewById<TextView>(R.id.dateTextView)
                            val userFullNameTextView = historyView.findViewById<TextView>(R.id.userFullName)
                            val descriptionTextView = historyView.findViewById<TextView>(R.id.descriptionTextView)
                            val treatmentPlanTextView = historyView.findViewById<TextView>(R.id.treatmentPlanTextView)
                            val editButton = historyView.findViewById<Button>(R.id.editButton)

                            userFullNameTextView.text = fullName
                            descriptionTextView.text = description
                            treatmentPlanTextView.text = treatmentPlan

                            // Formatowanie daty na format dd.MM.yyyy HH:mm i ustawienie tekstu
                            if (date != null) {
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("pl", "PL"))
                                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Warsaw")
                                dateTextView.text = getString(
                                    R.string.HistoryTreatmentDate,
                                    dateFormat.format(date.toDate())
                                )
                            } else {
                                dateTextView.text = getString(R.string.DateError)
                            }

                            // Obsługa przycisku Edycji
                            editButton.setOnClickListener {
                                showEditMedicalHistoryDialog(document.id, description, treatmentPlan)
                            }

                            medicalHistoryLayout.addView(historyView)
                        }
                }
            }
    }


    private fun showAddMedicalHistoryDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medical_history, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val treatmentPlanEditText = dialogView.findViewById<EditText>(R.id.treatmentPlanEditText)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.AddHistoryTreatment2))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.Add)) { _, _ ->
                val description = descriptionEditText.text.toString()
                val treatmentPlan = treatmentPlanEditText.text.toString()
                addMedicalHistoryEntry(description, treatmentPlan)
            }
            .setNegativeButton(getString(R.string.Cancel), null)
            .create()

        dialog.show()
    }

    private fun addMedicalHistoryEntry(description: String, treatmentPlan: String) {
        if (userId == null) return

        val medicalHistory = hashMapOf(
            "userId" to userId!!,
            "date" to Timestamp.now(),
            "description" to description,
            "treatmentPlan" to treatmentPlan
        )

        db.collection("medicalHistory")
            .add(medicalHistory)
            .addOnSuccessListener {
                // Po dodaniu wpisu, odśwież widok
                medicalHistoryLayout.removeAllViews()
                loadMedicalHistory(userId!!)
            }
    }

    private fun showEditMedicalHistoryDialog(documentId: String, currentDescription: String?, currentTreatmentPlan: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_medical_history, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val treatmentPlanEditText = dialogView.findViewById<EditText>(R.id.treatmentPlanEditText)

        // Ustawienie aktualnych wartości
        descriptionEditText.setText(currentDescription)
        treatmentPlanEditText.setText(currentTreatmentPlan)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.EditHistoryTreatment))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.Save)) { _, _ ->
                val updatedDescription = descriptionEditText.text.toString()
                val updatedTreatmentPlan = treatmentPlanEditText.text.toString()
                updateMedicalHistoryEntry(documentId, updatedDescription, updatedTreatmentPlan)
            }
            .setNegativeButton(getString(R.string.CancelTreatment), null)
            .create()

        dialog.show()
    }

    private fun updateMedicalHistoryEntry(documentId: String, description: String, treatmentPlan: String) {
        val updatedData = mapOf(
            "description" to description,
            "treatmentPlan" to treatmentPlan
        )

        db.collection("medicalHistory").document(documentId)
            .update(updatedData)
            .addOnSuccessListener {

                medicalHistoryLayout.removeAllViews()
                loadMedicalHistory(userId!!)
            }
    }

    companion object {
        fun newInstance(userId: String): UserMedicalHistoryFragment {
            val fragment = UserMedicalHistoryFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
