package com.azimjonc.projects.caffeeshop

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azimjonc.projects.caffeeshop.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    //    View binding
    private lateinit var binding: ActivityRegisterBinding

    //    Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //    progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

//    init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance()


//        init progress dialog, will show while creating account | Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

//        handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

//        handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*Steps
            * 1) Input data
            * 2) Validate data
            * 3) Create Account - Firebase Auth
            * 4) Save User Info - Firebase RealTime Database*/
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
//         1) Input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

//        2) Validate data
        if (name.isEmpty()) {
//            Empty name...
            Toast.makeText(this, "Enter your name...", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email pattern
            Toast.makeText(this, "Invalid Email Pattern...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            // empty password
            Toast.makeText(this, "Enter password...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            // empty confirm password
            Toast.makeText(this, "Confirm password...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Password doesn't match...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        // 3) Create Account - Firebase Auth


//        show progress
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

//        create user in Firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)

            .addOnSuccessListener {
//                account created, now add user info in db
                updateUserInfo()
            }
            .addOnFailureListener { e ->
//                failure creating account
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }

    }

    private fun updateUserInfo() {
//        4) Save User Info - Firebase RealTime Database
        progressDialog.setMessage("Saving user info...")

//        timestamp
        val timestamp = System.currentTimeMillis()

//        get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid

//        setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // add empty, will do in profile edit
        hashMap["userType"] =
            "user" // possible values are user/admin, will change value to admin manually on firebase bd
        hashMap["timestamp"] = timestamp

//        set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
//                user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Account created...",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
//                Failed adding data
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed saving user info due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}