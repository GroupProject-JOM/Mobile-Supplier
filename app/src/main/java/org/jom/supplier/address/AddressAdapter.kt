package org.jom.supplier.address

import org.jom.supplier.R

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

data class AddressItem(
    val id: Int,
    val estate: String,
    val address: String
)

class AddressAdapter(private val addressItems: List<AddressItem>) :
    RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addressTitle: TextView = itemView.findViewById(R.id.address_title)
        val addressText: TextView = itemView.findViewById(R.id.address_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_address_card, parent, false)
        return AddressViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val currentItem = addressItems[position]

        holder.addressTitle.text = currentItem.estate
        holder.addressText.text = currentItem.address

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewAddressActivity::class.java)

            // Pass data to the next activity if needed
            intent.putExtra("id", currentItem.id)
            holder.itemView.context.startActivity(intent)
        }

        // Display a toast with all the data when the card is clicked
        holder.itemView.setOnLongClickListener {
            val toastMessage =
                "Area: ${currentItem.address}"
            Toast.makeText(holder.itemView.context, toastMessage, Toast.LENGTH_SHORT).show()

            true
        }
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }
}
