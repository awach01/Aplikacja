package com.example.aplikacjagabinet.AdminPanel

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacjagabinet.R
import com.example.aplikacjagabinet.User
import com.example.aplikacjagabinet.UserAdapter
import com.google.firebase.firestore.FirebaseFirestore

class UserListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var searchEditText: EditText
    private val db = FirebaseFirestore.getInstance()
    private val users = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = UserAdapter(users, ::onUserClick)
        recyclerView.adapter = adapter

        loadUsers()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterUsers(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                users.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    users.add(user)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun filterUsers(query: String) {
        val filteredUsers = users.filter {
            it.firstName.contains(query, ignoreCase = true) ||
                    it.lastName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        }
        adapter.updateList(filteredUsers)
    }

    private fun onUserClick(userId: String) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val fragment = UserDetailFragment.newInstance(userId)
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
