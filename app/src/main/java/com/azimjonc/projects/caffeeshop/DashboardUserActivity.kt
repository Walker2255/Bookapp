package com.azimjonc.projects.caffeeshop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azimjonc.projects.caffeeshop.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {

    //    view binding
    private lateinit var binding: ActivityDashboardUserBinding


    //    Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        init firebase  auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //        handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }

    private fun checkUser() {
//        get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
//            not logged, user can stay in user dashboard without login too
            binding.subTitleTv.text = "Not Logged In"
        } else {
//            logged in, get and show user info
            val email = firebaseUser.email
//            set to text view  of toolbar

            binding.subTitleTv.text = email

        }
    }
}