package com.example.aplikacjagabinet.Calendar

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aplikacjagabinet.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class CalendarFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarView = view.findViewById(R.id.calendarView)

        // Set up calendar to show only future dates
        calendarView.state().edit()
            .setMinimumDate(CalendarDay.today())
            .commit()

        // Add decorators for past dates, weekends, today, available and fully booked days
        calendarView.addDecorator(PastDatesDecorator())
        calendarView.addDecorator(WeekendDecorator())
        calendarView.addDecorator(TodayDecorator())
        calendarView.addDecorator(WeekdayDecorator())

        loadCalendarDecorators()

        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                if (isWeekday(date)) {
                    showTimePickerDialog(date)
                } else {
                    Toast.makeText(context, getString(R.string.ChooseWorkingDay), Toast.LENGTH_SHORT).show()
                }
            }
        })

        return view
    }

    fun isWeekday(date: CalendarDay): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month - 1, date.day)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
    }

    private fun getOccupiedSlotsForDate(date: CalendarDay, callback: (List<Int>) -> Unit) {
        val dateString = dateFormat.format(Date(date.year - 1900, date.month - 1, date.day))
        db.collection("appointments")
            .whereEqualTo("date2", dateString)
            .get()
            .addOnSuccessListener { documents ->
                val slots = documents.mapNotNull { it.getLong("timeSlot")?.toInt() }
                callback(slots)
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarFragment", "Error getting documents: ", exception)
                callback(emptyList())
            }
    }

    private fun showTimePickerDialog(date: CalendarDay) {
        getOccupiedSlotsForDate(date) { occupiedSlots ->
            val hours = (8..19).map { it to "${it}:00 - ${it + 1}:00" }.toMap()
            val items = hours.values.toTypedArray()
            val currentHour = getCurrentHourInPoland()

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.ChooseTimeAppointment))

            val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val hour = position + 8
                    if (date == CalendarDay.today() && hour <= currentHour) {
                        view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                        view.isEnabled = false
                        view.isClickable = false
                    } else if (hour in occupiedSlots) {
                        view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                    } else {
                        view.setBackgroundColor(ContextCompat.getColor(context,
                            R.color.darker_green
                        ))
                    }
                    return view
                }

                override fun isEnabled(position: Int): Boolean {
                    val hour = position + 8
                    // Disable clicking for past hours on today's date
                    return !(date == CalendarDay.today() && hour <= currentHour)
                }
            }

            builder.setAdapter(adapter) { dialog, which ->
                val selectedHour = which + 8
                if (selectedHour in occupiedSlots) {
                    Toast.makeText(
                        context,
                        getString(R.string.BusyAppointment),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveAppointment(date, selectedHour)
                }
            }

            builder.create().show()
        }
    }

    private fun getCurrentHourInPoland(): Int {
        val timeZone = TimeZone.getTimeZone("Europe/Warsaw")
        val calendar = Calendar.getInstance(timeZone)
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun saveAppointment(date: CalendarDay, timeSlot: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month - 1, date.day, timeSlot, 0)
        val timestamp = Timestamp(calendar.time)

        val dateString = dateFormat.format(calendar.time)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "unknown"

        val appointment = hashMapOf(
            "userId" to currentUser?.uid,
            "date" to timestamp,
            "date2" to dateString,
            "timeSlot" to timeSlot,
            "duration" to 1,
            "status" to "confirmed"
        )

        db.collection("appointments")
            .add(appointment)
            .addOnSuccessListener {
                Toast.makeText(context, getString(R.string.AppointmentSaved), Toast.LENGTH_SHORT).show()
                sendConfirmationEmail(userEmail, timestamp.toDate().toString(), timeSlot)
                loadCalendarDecorators()  // Update decorators after saving the appointment
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,
                    getString(R.string.AppointmentSavedError), Toast.LENGTH_SHORT).show()
                Log.w("CalendarFragment", "Error adding document", e)
            }
    }

    private fun sendConfirmationEmail(userEmail: String, date: String, timeSlot: Int) {
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
                subject = getString(R.string.AppointmentConfirmEmail)
                setText("Twoja wizyta została zapisana na $date o godzinie ${timeSlot}:00.")
            }

            Thread {
                try {
                    Transport.send(message)
                } catch (e: Exception) {
                    Log.e("CalendarFragment", "Error sending email", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("CalendarFragment", "Error sending email", e)
        }
    }

    private fun loadCalendarDecorators() {
        db.collection("appointments")
            .get()
            .addOnSuccessListener { documents ->
                val availableDates = mutableSetOf<CalendarDay>()
                val fullyBookedDates = mutableSetOf<CalendarDay>()
                val allDates = mutableMapOf<CalendarDay, MutableSet<Int>>()

                for (document in documents) {
                    val dateTimestamp = document.getTimestamp("date")
                    val calendar = Calendar.getInstance()
                    calendar.time = dateTimestamp?.toDate() ?: continue
                    val calendarDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                    val timeSlot = document.getLong("timeSlot")?.toInt() ?: continue

                    if (isWeekday(calendarDay) && calendarDay.isAfter(CalendarDay.today())) {
                        allDates.getOrPut(calendarDay) { mutableSetOf() }.add(timeSlot)
                    }
                }

                for ((day, slots) in allDates) {
                    if (slots.size == 12) {
                        fullyBookedDates.add(day)
                    } else {
                        availableDates.add(day)
                    }
                }

                if (isAdded) {
                    // Add decorators for available and fully booked days
                    calendarView.addDecorator(AvailableDatesDecorator(availableDates))
                    calendarView.addDecorator(FullyBookedDatesDecorator(fullyBookedDates))
                }
            }
            .addOnFailureListener { exception ->
                Log.w("CalendarFragment", "Error getting documents: ", exception)
            }
    }

    inner class PastDatesDecorator : DayViewDecorator {
        private val today = CalendarDay.today()
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.isBefore(today)
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.past_date_background
                )!!)
            }
        }
    }

    inner class WeekendDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val calendar = Calendar.getInstance()
            calendar.set(day.year, day.month - 1, day.day)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.past_date_background
                )!!)
            }
        }
    }

    inner class TodayDecorator : DayViewDecorator {
        private val today = CalendarDay.today()
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day == today
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.today_background
                )!!)
            }
        }
    }

    inner class WeekdayDecorator : DayViewDecorator {
        private val today = CalendarDay.today()
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return isWeekday(day) && day.isAfter(today)
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.weekday_background
                )!!)
            }
        }
    }

    inner class AvailableDatesDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.available_date_background
                )!!)
            }
        }
    }

    inner class FullyBookedDatesDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            if (isAdded) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                    R.drawable.fully_booked_date_background
                )!!)
            }
        }
    }
}
