package com.example.aplikacjagabinet.Calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagabinet.AdminPanel.UserDetailFragment
import com.example.aplikacjagabinet.Appointments.Appointment
import com.example.aplikacjagabinet.Appointments.AppointmentAdapter
import com.example.aplikacjagabinet.R
import com.google.firebase.firestore.FirebaseFirestore

class DayDetailFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_day_detail, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)

        val date = arguments?.getString("date") ?: ""

        recyclerView.layoutManager = LinearLayoutManager(context)

        loadAppointments(date)

        return view
    }

    private fun loadAppointments(date: String) {
        db.collection("appointments")
            .whereEqualTo("date2", date)
            .orderBy("timeSlot")
            .get()
            .addOnSuccessListener { documents ->
                val appointments = mutableListOf<Appointment>()
                val userIds = mutableSetOf<String>()

                for (document in documents) {
                    val appointment = document.toObject(Appointment::class.java)
                    appointments.add(appointment)
                    userIds.add(appointment.userId)
                }

                if (appointments.isEmpty()) {
                    Toast.makeText(context,
                        getString(R.string.DayNoAppointments), Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                    return@addOnSuccessListener
                }

                loadUserNames(userIds) { userNameMap ->
                    val appointmentsWithNames = appointments.map { appointment ->
                        appointment.userName = userNameMap[appointment.userId] ?: "Unknown User"
                        appointment
                    }
                    recyclerView.adapter = AppointmentAdapter(appointmentsWithNames, ::onUserClick, ::onCancelClick)
                }
            }
    }

    private fun loadUserNames(userIds: Set<String>, callback: (Map<String, String>) -> Unit) {
        val userNameMap = mutableMapOf<String, String>()

        db.collection("users").whereIn("userId", userIds.toList()).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userId = document.id
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

    private fun onUserClick(userId: String) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val fragment = UserDetailFragment.newInstance(userId)
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun onCancelClick(appointment: Appointment) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.CancelConfirm2))
            .setMessage(getString(R.string.CancelConfrimQuestion))
            .setPositiveButton("Tak") { _, _ ->
                cancelAppointment(appointment)
            }
            .setNegativeButton("Nie", null)
            .show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        db.collection("appointments")
            .whereEqualTo("date2", appointment.date2)
            .whereEqualTo("timeSlot", appointment.timeSlot)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
                Toast.makeText(context, getString(R.string.AppointmentCancelled), Toast.LENGTH_SHORT).show()
                loadAppointments(appointment.date2) // reload appointments after cancel
            }
            .addOnFailureListener {
                Toast.makeText(context,
                    getString(R.string.AppointmentCancelledError), Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(date: String): DayDetailFragment {
            val fragment = DayDetailFragment()
            val args = Bundle()
            args.putString("date", date)
            fragment.arguments = args
            return fragment
        }
    }
}
