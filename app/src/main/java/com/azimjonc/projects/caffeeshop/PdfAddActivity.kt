package com.azimjonc.projects.caffeeshop

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.azimjonc.projects.caffeeshop.databinding.ActivityPdfAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityPdfAddBinding

    // Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    // ArrayList to hold PDF categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // URI of picked PDF
    private var pdfUri: Uri? = null

    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // Setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // Handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        // Handle click, pick PDF intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // Handle click, start uploading PDF/book
        binding.submitBtn.setOnClickListener {
            /* Step1: Validate data
             * Step2: Upload PDF to Firebase Storage
             * Step3: Get URL of uploaded PDF
             * Step4: Upload PDF info to Firebase DB */
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // Step1: Validate data
        Log.d(TAG, "validateData: validating data")

        // Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // Validate data
        when {
            title.isEmpty() -> Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
            description.isEmpty() -> Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
            category.isEmpty() -> Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show()
            pdfUri == null -> Toast.makeText(this, "Pick PDF...", Toast.LENGTH_SHORT).show()
            else -> uploadPdfToStorage() // Data validated, begin upload
        }
    }

    private fun uploadPdfToStorage() {
        // Step2: Upload PDF to Firebase Storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")

        // Show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        // Timestamp
        val timestamp = System.currentTimeMillis()

        // Path of PDF in Firebase Storage
        val filePathAndName = "Books/$timestamp"
        // Storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: PDF uploaded, now getting URL...")

                // Step3: Get URL of uploaded PDF
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val uploadedPdfUrl = uri.toString()
                    uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
                }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "uploadPdfToStorage: failed to get URL due to ${e.message}")
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to get URL due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // Step4: Upload PDF info to Firebase DB
        Log.d(TAG, "uploadPdfInfoToDb: uploading to DB")
        progressDialog.setMessage("Uploading PDF info...")

        // UID of current user
        val uid = firebaseAuth.uid

        // Setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        // DB reference DB > BookId > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to DB")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded...", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: loading PDF categories")
        // Init ArrayList
        categoryArrayList = ArrayList()

        // DB reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // Get data
                    val model = ds.getValue(ModelCategory::class.java)
                    // Add to ArrayList
                    if (model != null) {
                        categoryArrayList.add(model)
                        Log.d(TAG, "onDataChange: ${model.category}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadPdfCategories: failed to load categories due to ${error.message}")
            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing PDF category pick dialog")

        // Get string array of categories from ArrayList
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // Alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                // Handle item click
                // Get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                // Set category to TextView
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }.show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting PDF pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    private val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                result.data?.data?.let { uri ->
                    pdfUri = uri
                    Log.d(TAG, "PDF URI: $pdfUri")
                }
            } else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
