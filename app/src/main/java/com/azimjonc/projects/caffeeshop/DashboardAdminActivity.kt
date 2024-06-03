package com.azimjonc.projects.caffeeshop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azimjonc.projects.caffeeshop.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardAdminActivity : AppCompatActivity() {

    //    viewbinding
    private lateinit var binding: ActivityDashboardAdminBinding


    //    Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


//        handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }


    }

    private fun checkUser() {
//        get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
//            not logged, in goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
//            logged in, get and show user info
            val email = firebaseUser.email
//            set to text view  of toolbar

            binding.subTitleTv.text = email

        }
    }
}