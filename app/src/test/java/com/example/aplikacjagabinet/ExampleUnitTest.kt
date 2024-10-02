import com.example.aplikacjagabinet.Calendar.CalendarFragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class CalendarWeekdayTest {

    private lateinit var calendarFragment: CalendarFragment

    @Before
    fun setUp() {
        calendarFragment = CalendarFragment() // Stworzenie instancji fragmentu
    }

    @Test
    fun testIsWeekday() {
        // Poniedziałek - 9 września 2024 (powinien być dniem roboczym)
        val monday = CalendarDay.from(2024, Calendar.SEPTEMBER, 9)
        assertTrue(calendarFragment.isWeekday(monday))

        // Sobota - 7 września 2024 (nie powinien być dniem roboczym)
        val saturday = CalendarDay.from(2024, Calendar.SEPTEMBER, 7)
        assertFalse(calendarFragment.isWeekday(saturday))

        // Niedziela - 8 września 2024 (nie powinien być dniem roboczym)
        val sunday = CalendarDay.from(2024, Calendar.SEPTEMBER, 8)
        assertFalse(calendarFragment.isWeekday(sunday))

        // Wtorek - 10 września 2024 (powinien być dniem roboczym)
        val tuesday = CalendarDay.from(2024, Calendar.SEPTEMBER, 10)
        assertTrue(calendarFragment.isWeekday(tuesday))
    }
}
