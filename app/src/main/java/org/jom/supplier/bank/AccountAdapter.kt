package org.jom.supplier.bank

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.jom.supplier.R

data class AccountItem(
    val id: Int,
    val account_number: String,
    val nickName: String,
)

class AccountAdapter(private val accountItems: List<AccountItem>) :
    RecyclerView.Adapter<AccountAdapter.AccountsViewHolder>() {

    inner class AccountsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val accountTitle: TextView = itemView.findViewById(R.id.account_title)
        val accountText: TextView = itemView.findViewById(R.id.account_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccountAdapter.AccountsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_card, parent, false)
        return AccountsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountAdapter.AccountsViewHolder, position: Int) {
        val currentItem = accountItems[position]

        holder.accountTitle.text = currentItem.nickName
        holder.accountText.text = currentItem.account_number

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewBankActivity::class.java)

            // Pass data to the next activity if needed
            intent.putExtra("id", currentItem.id)
            holder.itemView.context.startActivity(intent)
        }

        // Display a toast with all the data when the card is clicked
        holder.itemView.setOnLongClickListener {
            val toastMessage =
                "NickName: ${currentItem.nickName}"
            Toast.makeText(holder.itemView.context, toastMessage, Toast.LENGTH_SHORT).show()

            true
        }
    }

    override fun getItemCount(): Int {
        return accountItems.size
    }

}