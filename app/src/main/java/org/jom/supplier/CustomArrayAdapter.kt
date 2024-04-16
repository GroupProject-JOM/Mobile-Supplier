package org.jom.supplier

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import okhttp3.ResponseBody
import retrofit2.Callback

class CustomArrayAdapter(
    context: Context,
    resource: Int,
    objects: List<String>, // or Array<String> depending on your needs
    private val customIds: List<Long> // Assuming you want to pass IDs along with the strings
) : ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (position == 0) {
            // Set the default text for the first item
            (view as TextView).setTextColor(Color.GRAY)
        }
        return view
    }

    override fun getItemId(position: Int): Long {
        // Return custom ID instead of position
        return customIds[position]
    }
}