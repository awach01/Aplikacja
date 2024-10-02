package com.example.myapplication.Appointments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class AppointmentAdapter(
    private val appointments: List<Appointment>,
    private val onUserClick: (String) -> Unit,
    private val onCancelClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    class AppointmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val appointmentDate: TextView = view.findViewById(R.id.appointmentDate)
        val appointmentTime: TextView = view.findViewById(R.id.appointmentTime)
        val cancelAppointmentButton: Button = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]

        holder.userName.text = appointment.userName
        holder.appointmentDate.text = appointment.date2
        holder.appointmentTime.text = convertTimeSlotToTime(appointment.timeSlot)

        holder.itemView.setOnClickListener {
            onUserClick(appointment.userId)
        }

        holder.cancelAppointmentButton.setOnClickListener {
            onCancelClick(appointment)
        }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    private fun convertTimeSlotToTime(timeSlot: Int): String {
        val startHour = timeSlot
        return String.format("%02d:00 - %02d:00", startHour, startHour + 1)
    }
}
