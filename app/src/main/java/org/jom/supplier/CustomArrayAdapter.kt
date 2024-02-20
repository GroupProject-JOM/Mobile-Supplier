package org.jom.supplier

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomArrayAdapter(context: Context, resource: Int, private val objects: List<String>, private val customIds: List<Long>) :
    ArrayAdapter<String>(context, resource, objects) {

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