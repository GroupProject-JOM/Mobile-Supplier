package org.jom.supplier.chat

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import org.jom.supplier.Methods
import org.jom.supplier.R

data class ChatItem(
    val id: Int,
    val sender: Int,
    val recipient: Int,
    val content: String,
)


class ChatAdapter(private val chatItems: List<ChatItem>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.message_text)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout =
            R.layout.sent_message // Assuming there's a common layout for both sent and received messages
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentItem = chatItems[position]

        // Determine if the message is sent or received
        val isSent = isSender(currentItem.sender)

        // Set the message text
        holder.message.text = currentItem.content

        // Set the background color based on message type
        val backgroundColor = if (isSent) R.drawable.sent_msg else R.drawable.recieved_msg
        holder.itemView.setBackgroundResource(backgroundColor)

        // Set margins based on message type
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        val marginStart = if (isSent) 120 else 20
        val marginEnd = if (isSent) 20 else 120
        val bottomMargin = 10
        layoutParams.marginStart = dpToPx(holder.itemView.context, marginStart)
        layoutParams.marginEnd = dpToPx(holder.itemView.context, marginEnd)
        layoutParams.bottomMargin = dpToPx(holder.itemView.context, bottomMargin)
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return chatItems.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUser(): Int {
        lateinit var jwt: String

        // get instance of methods class
        val methods = Methods()

        // get cookie operations
        val cookieManager = CookieManager.getInstance()
        val cookies = methods.getAllCookies(cookieManager)

        // get jwt from cookie
        for (cookie in cookies) {
            if (cookie.first == "jwt") {
                jwt = cookie.second
            }
        }

        //WebSocket
        var payload = methods.getPayload(jwt);

        // assign values to socket operation variables

        return methods.floatToInt(payload["user"])
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isSender(position: Int): Boolean {
        return chatItems[position].sender == getUser()
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale).toInt()
    }
}
