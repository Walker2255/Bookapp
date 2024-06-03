package com.azimjonc.projects.caffeeshop

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.azimjonc.projects.caffeeshop.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding


    //    Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({

            checkUser()
        }, 1000)
    }

    private fun checkUser() {
//        get current user, if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
//            user not  logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
//            user logged in, check user type, same as done in login screen
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    get user type e.g. user or admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
//                        its simple user, open user dashboard
                        startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                        finish()

                    } else if (userType == "admin") {
//                        its admin, open admin dashboard
                        startActivity(
                            Intent(
                                this@SplashActivity, DashboardAdminActivity::class.java
                            )
                        )
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
    }
}

/* Keep user logged in
* 1) Check if user logged in
* 2) Check type of user */