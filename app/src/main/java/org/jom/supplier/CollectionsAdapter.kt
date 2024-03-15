package org.jom.supplier

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.jom.supplier.supply.ViewSupplyActivity

data class CollectionItem(
    val id: Int,
    val time: String,
    val amount: String,
    val method: String,
    val status: String
)


class CollectionsAdapter(private val collectionItems: List<CollectionItem>) :
    RecyclerView.Adapter<CollectionsAdapter.CollectionsViewHolder>() {

    inner class CollectionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val collectionId: TextView = itemView.findViewById(R.id.supply_id)
        val collectionTime: TextView = itemView.findViewById(R.id.time)
        val collectionAmount: TextView = itemView.findViewById(R.id.coconut_amount)
        val collectionSupply: TextView = itemView.findViewById(R.id.supply_method)

        val collectionPaymentIcon: ImageView = itemView.findViewById(R.id.icon_cash)
        val statusButton: Button = itemView.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection_card, parent, false)
        return CollectionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionsViewHolder, position: Int) {
        val currentItem = collectionItems[position]

        holder.collectionId.text =
            "S/" + currentItem.method.capitalize()[0] + "/" + currentItem.id
        holder.collectionTime.text = currentItem.time
        holder.collectionAmount.text = currentItem.amount
        holder.collectionSupply.text = currentItem.method.capitalize()
        holder.statusButton.setText(currentItem.status.capitalize())

        if (currentItem.method == "yard") {
            holder.collectionPaymentIcon.setImageResource(R.drawable.icon_droppin)
        }

        if (currentItem.status == "pending") {
            holder.statusButton.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.lightSidebarColor
                )
            )
        } else if (currentItem.status == "accept") {
            holder.statusButton.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.lightAcceptYellow
                )
            )
        } else if (currentItem.status == "ready") {
            holder.statusButton.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.lightCompletedGreen
                )
            )
        } else if (currentItem.status == "rejected") {
            holder.statusButton.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.lightRejectRed
                )
            )
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewSupplyActivity::class.java)

            // Pass data to the next activity if needed
            intent.putExtra("id", currentItem.id)
//            intent.putExtra("collectionTime", currentItem.time)
//            intent.putExtra("collectionAmount", currentItem.amount)

            holder.itemView.context.startActivity(intent)
        }

        // Display a toast with all the data when the card is clicked
        holder.itemView.setOnLongClickListener {
            val toastMessage =
                "Area: ${currentItem.id}\nTime: ${currentItem.time}\nAmount: ${currentItem.amount}"
            Toast.makeText(holder.itemView.context, toastMessage, Toast.LENGTH_SHORT).show()

            true
        }

        // Set your cash icon here, you might want to use a library like Glide or Picasso
        // depending on how you handle your image resources.
//         holder.cashIcon.setImageResource(R.drawable.ic_cash)

//        holder.selectButton.setOnClickListener {
//            // Handle button click here if needed
//        }
    }

    override fun getItemCount(): Int {
        return collectionItems.size
    }
}
