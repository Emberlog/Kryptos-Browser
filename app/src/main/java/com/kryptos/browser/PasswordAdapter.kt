package com.kryptos.browser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SavedPassword(val id: Int, val domain: String, val name: String, val username: String, val password: String)

class PasswordAdapter(
    private val passwords: List<SavedPassword>,
    private val onDeleteClick: (SavedPassword) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val domain: TextView = view.findViewById(R.id.pm_domain)
        val username: TextView = view.findViewById(R.id.pm_username)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_password)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_password, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = passwords[position]
        holder.domain.text = item.domain
        holder.username.text = item.username

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = passwords.size
}
