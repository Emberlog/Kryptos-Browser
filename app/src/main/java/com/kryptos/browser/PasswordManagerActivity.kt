package com.kryptos.browser

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PasswordManagerActivity : AppCompatActivity() {

    private lateinit var db: PasswordDatabaseHelper
    private lateinit var recycler: RecyclerView
    private lateinit var txtEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_manager)

        db = PasswordDatabaseHelper(this)
        recycler = findViewById(R.id.passwords_recycler_view)
        txtEmpty = findViewById(R.id.txt_empty_passwords)
        recycler.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.btn_back_pm).setOnClickListener {
            finish()
        }

        loadPasswords()
    }

    private fun loadPasswords() {
        val list = db.getAllPasswords()

        if (list.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            txtEmpty.visibility = View.GONE
            recycler.visibility = View.VISIBLE
        }

        recycler.adapter = PasswordAdapter(list,
            onDeleteClick = { item ->
                showDeleteDialog(item)
            }
        )
    }

    private fun showDeleteDialog(item: SavedPassword) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val message = dialog.findViewById<TextView>(R.id.dialog_message)
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_dialog_cancel)
        val btnConfirm = dialog.findViewById<TextView>(R.id.btn_dialog_confirm)

        title.text = "Delete Password"
        message.text = "Are you sure you want to delete the password for ${item.domain}?"
        btnConfirm.text = "Delete"
        btnConfirm.setTextColor(Color.parseColor("#EF4444"))

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            db.deletePassword(item.domain)
            Toast.makeText(this, "Password Deleted", Toast.LENGTH_SHORT).show()
            loadPasswords()
        }
        dialog.show()
    }
}
