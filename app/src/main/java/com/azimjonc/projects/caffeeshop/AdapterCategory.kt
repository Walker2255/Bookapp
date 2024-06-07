package com.azimjonc.projects.caffeeshop

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.azimjonc.projects.caffeeshop.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory(
    private val context: Context,
    public var categoryArrayList: ArrayList<ModelCategory>
) : Adapter<AdapterCategory.HolderCategory>(), Filterable {

    private var filterList: ArrayList<ModelCategory> = ArrayList(categoryArrayList)
    private var filter: FilterCategory? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        val binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding)
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size // number of items in list
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        // set data
        holder.categoryTv.text = category

        // handle click, delete category
        holder.deleteBtn.setOnClickListener {
            // confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Confirm") { _, _ ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.show()  // show the dialog
        }
        // handle click, start pdf list admin activity, also pas pdf id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)

        }
    }

    private fun deleteCategory(model: ModelCategory) {
        // get id of category to delete
        val id = model.id
        // Firebase DB > Categories > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    // ViewHolder class to hold/init UI views for row_category.xml
    inner class HolderCategory(binding: RowCategoryBinding) : ViewHolder(binding.root) {
        // init UI views
        val categoryTv: TextView = binding.categoryTv
        val deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }
}
