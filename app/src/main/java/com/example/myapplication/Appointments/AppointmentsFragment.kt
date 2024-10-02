package com.example.myapplication.Appointments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class AppointmentsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var noAppointmentsText: TextView
    private lateinit var adapter: AppointmentAdapter
    private val appointmentsList = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointments, container, false)
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppointmentAdapter(appointmentsList, {}, ::onCancelAppointmentClick)
        recyclerView.adapter = adapter

        loadAppointments()

        return view
    }

    private fun loadAppointments() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("appointments")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val userIds = documents.mapNotNull { it.getString("userId") }
                        loadUserNames(userIds) { userNameMap ->
                            displayAppointments(documents, userNameMap)
                        }
                    } else {
                        noAppointmentsText.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("AppointmentsFragment", "Error getting documents: ", exception)
                    Toast.makeText(context,
                        getString(R.string.ErrorDownloadingAppointments), Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, getString(R.string.NotLogged), Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserNames(userIds: List<String>, callback: (Map<String, String>) -> Unit) {
        db.collection("users")
            .whereIn("userId", userIds)
            .get()
            .addOnSuccessListener { documents ->
                val userNameMap = mutableMapOf<String, String>()
                for (document in documents) {
                    val userId = document.getString("userId") ?: continue
                    val firstName = document.getString("firstName") ?: "Unknown"
                    val lastName = document.getString("lastName") ?: "User"
                    userNameMap[userId] = "$firstName $lastName"
                }
                callback(userNameMap)
            }
            .addOnFailureListener {
                callback(emptyMap())
            }
    }

    private fun displayAppointments(documents: QuerySnapshot, userNameMap: Map<String, String>) {
        appointmentsList.clear()
        for (document in documents) {
            val appointment = document.toObject(Appointment::class.java)
            appointment.userName = userNameMap[appointment.userId] ?: "Unknown User"
            appointmentsList.add(appointment)
        }

        appointmentsList.sortWith(compareBy({ it.date2 }, { it.timeSlot }))
        adapter.notifyDataSetChanged()
    }


    private fun onCancelAppointmentClick(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.CancelConfirm))
            .setMessage(getString(R.string.QuestionCancelConfirm))
            .setPositiveButton(getString(R.string.AppointYes)) { _, _ ->
                cancelAppointment(appointment)
            }
            .setNegativeButton(getString(R.string.AppointNo), null)
            .show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: "unknown"
        db.collection("appointments")
            .whereEqualTo("date2", appointment.date2)
            .whereEqualTo("timeSlot", appointment.timeSlot)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
                sendCancellationEmail(userEmail, appointment.date2, appointment.timeSlot)
                navigateToAppointmentCancelledFragment()
            }
            .addOnFailureListener { e ->
                Log.w("AppointmentsFragment", "Error deleting document", e)
                Toast.makeText(context,
                    getString(R.string.ErrorCancelAppointment), Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendCancellationEmail(userEmail: String, date: String, timeSlot: Int) {
        val username = "gabinetpraca@outlook.com"
        val password = "Klopik123"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp-mail.outlook.com")
            put("mail.smtp.port", "587")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(userEmail)
                )
                subject = "Anulowanie wizyty"
                setText(getString(R.string.SuccessCancelAppointment, date, timeSlot))
            }

            Thread {
                try {
                    Transport.send(message)
                } catch (e: Exception) {
                    Log.e("AppointmentsFragment", "Error sending email", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("AppointmentsFragment", "Error sending email", e)
        }
    }

    private fun navigateToAppointmentCancelledFragment() {
        val cancelledFragment = AppointmentCancelledFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container, cancelledFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
