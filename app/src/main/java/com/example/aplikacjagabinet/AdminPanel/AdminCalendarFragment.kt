package com.example.aplikacjagabinet.AdminPanel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.aplikacjagabinet.Calendar.DayDetailFragment
import com.example.aplikacjagabinet.R
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.Calendar

class AdminCalendarFragment : Fragment() {

    private lateinit var calendarView: MaterialCalendarView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_calendar, container, false)
        calendarView = view.findViewById(R.id.adminCalendarView)
        val buttonUserList: Button = view.findViewById(R.id.buttonUserList)

        // Dekoratory
        calendarView.addDecorator(WeekendDecorator())
        calendarView.addDecorator(TodayDecorator())
        calendarView.addDecorator(WeekdayDecorator())

        loadCalendarDecorators()

        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            if (selected) {
                val selectedDate = "${date.year}-${String.format("%02d", date.month)}-${String.format("%02d", date.day)}"
                checkAppointmentsForDate(selectedDate)
            }
        })

        buttonUserList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val fragment = UserListFragment()
            transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun isWeekday(date: CalendarDay): Boolean {
        val calendar = Calendar.getInstance()
        calendar.set(date.year, date.month - 1, date.day)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
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

                    if (isWeekday(calendarDay)) {
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

                    calendarView.addDecorator(TodayDecorator())
                    calendarView.addDecorator(AvailableDatesDecorator(availableDates))
                    calendarView.addDecorator(FullyBookedDatesDecorator(fullyBookedDates))
                    calendarView.invalidateDecorators()
                }
            }
    }

    private fun checkAppointmentsForDate(date: String) {
        db.collection("appointments")
            .whereEqualTo("date2", date)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(context,
                        getString(R.string.DayWithoutAppointments), Toast.LENGTH_SHORT).show()
                } else {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    val fragment = DayDetailFragment.newInstance(date)
                    transaction.replace(R.id.container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, getString(R.string.ErrorAppointments), Toast.LENGTH_SHORT).show()
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


    inner class WeekdayDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return isWeekday(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),
                R.drawable.weekday_background
            )!!)
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
}

