package com.example.aplikacjagabinet.Rest

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.example.aplikacjagabinet.R

class ContactFragment : Fragment() {

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactHours: TextView = view.findViewById(R.id.contact_hours)
        val contactDentist: TextView = view.findViewById(R.id.contact_dentist)
        val contactAssistant: TextView = view.findViewById(R.id.contact_assistant)
        val contactAddress: TextView = view.findViewById(R.id.contact_address)
        val contactPhone: TextView = view.findViewById(R.id.contact_phone)
        val contactEmail: TextView = view.findViewById(R.id.contact_email)

        contactHours.text = getString(R.string.WorkHoursContact)
        contactDentist.text = getString(R.string.DentistContact)
        contactAssistant.text = getString(R.string.AssistantContact)
        contactAddress.text = getString(R.string.AdressContact)
        contactPhone.text = getString(R.string.PhoneContact)
        contactEmail.text = getString(R.string.EmailContact)


        gestureDetector = GestureDetectorCompat(requireContext(), SwipeGestureListener())
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 != null && e2 != null) {
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun onSwipeRight() {
        parentFragmentManager.popBackStack()
        activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
