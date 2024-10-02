package com.example.aplikacjagabinet.Appointments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.aplikacjagabinet.Calendar.CalendarFragment

import com.example.aplikacjagabinet.R

class AppointmentCancelledFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointment_cancelled, container, false)

        view.setOnClickListener {
            navigateToCalendarFragment()
        }

        return view
    }

    private fun navigateToCalendarFragment() {
        val calendarFragment = CalendarFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container, calendarFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
