package com.example.aplikacjagabinet.Review

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagabinet.R
import com.example.aplikacjagabinet.ReviewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviews = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val reviewText: EditText = view.findViewById(R.id.review_text)
        val reviewRadioGroup: RadioGroup = view.findViewById(R.id.review_radio_group)
        val positiveRadioButton: RadioButton = view.findViewById(R.id.positive_radio_button)
        val submitButton: Button = view.findViewById(R.id.submit_button)
        val editButton: Button = view.findViewById(R.id.edit_button)

        reviewsRecyclerView = view.findViewById(R.id.reviews_recycler_view)
        reviewAdapter = ReviewAdapter(reviews)
        reviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        reviewsRecyclerView.adapter = reviewAdapter

        loadReviews()

        submitButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val text = reviewText.text.toString()
            val isPositive = reviewRadioGroup.checkedRadioButtonId == R.id.positive_radio_button

            if (text.isEmpty()) {
                Toast.makeText(context, getString(R.string.EnterOpinion), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName") ?: "Unknown"
                        val lastName = document.getString("lastName") ?: "User"
                        val review = Review(userId, firstName, lastName, isPositive, text)
                        db.collection("reviews").document(userId).set(review)
                            .addOnSuccessListener {
                                Toast.makeText(context,
                                    getString(R.string.OpinionSend), Toast.LENGTH_SHORT).show()
                                loadReviews()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context,
                                    getString(R.string.OpinionError, e.message), Toast.LENGTH_SHORT).show()
                                Log.e("ReviewFragment", "Error adding review", e)
                            }
                    } else {
                        Toast.makeText(context,
                            getString(R.string.OpinionErrorUser), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context,
                        getString(R.string.OpinionErrorUserDownload, e.message), Toast.LENGTH_SHORT).show()
                    Log.e("ReviewFragment", "Error fetching user data", e)
                }
        }

        editButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            db.collection("reviews").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val review = document.toObject(Review::class.java)
                        if (review != null) {
                            reviewText.setText(review.text)
                            if (review.positive) {
                                positiveRadioButton.isChecked = true
                            } else {
                                view.findViewById<RadioButton>(R.id.negative_radio_button).isChecked = true
                            }
                        }
                    } else {
                        Toast.makeText(context,
                            getString(R.string.NoOpinionEdit), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context,
                        getString(R.string.OpinionErrorDownload, e.message), Toast.LENGTH_SHORT).show()
                    Log.e("ReviewFragment", "Error fetching review", e)
                }
        }
    }

    private fun loadReviews() {
        db.collection("reviews").get()
            .addOnSuccessListener { documents ->
                reviews.clear()
                for (document in documents) {
                    val review = document.toObject(Review::class.java)
                    reviews.add(review)
                    Log.d("ReviewFragment", "Loaded review: $review")
                }
                reviewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context,
                    getString(R.string.OpinionErrorDownload2, e.message), Toast.LENGTH_SHORT).show()
                Log.e("ReviewFragment", "Error getting documents", e)
            }
    }
}
