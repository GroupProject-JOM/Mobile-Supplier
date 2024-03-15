package org.jom.supplier.supply

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import org.jom.supplier.CustomArrayAdapter
import org.jom.supplier.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PickupCashActivity : AppCompatActivity() {

    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var estate_location: Spinner
    private lateinit var date_icon: ImageView
    private lateinit var time_icon: ImageView
    private lateinit var next: Button

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_cash)

        // initialize date time Edit texts
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        date_icon = findViewById(R.id.date_icon)
        time_icon = findViewById(R.id.time_icon)

        // remove keyboard focus for date time inputs
        date.isFocusable = false
        time.isFocusable = false

        // date time icon and input fields click to popup date time pickers
        date.setOnClickListener {
            date_icon.callOnClick()
        }
        date_icon.setOnClickListener {
            showDatePicker()
        }
        time.setOnClickListener {
            time_icon.callOnClick()
        }

        // time picker
        time_icon.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                time.setText(SimpleDateFormat("HH:mm").format(calendar.time))
            }
            // time popup
            TimePickerDialog(
                this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()

        }

        // estate_location dropdown spinner
        estate_location = findViewById(R.id.estate_location)

        // values and ids list
        val customListEstate = listOf(
            Pair("Select Estate", -1L),  // Default item with custom ID -1
            Pair("Estate 1", 1L),
            Pair("Estate 2", 2L),
            Pair("Estate 3", 3L),
            Pair("Estate 4", 4L)
        )

        // map dropdown content to adapter
        val adapterEstate = CustomArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            customListEstate.map { it.first },
            customListEstate.map { it.second }
        )
        estate_location.adapter = adapterEstate

        // dropdown trigger
        estate_location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(
                    this@PickupCashActivity,
                    "You selected ${adapterView?.getItemAtPosition(position).toString()} \nId $id",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // net button
        next = findViewById(R.id.next)
        next.setOnClickListener{
            val intent = Intent(this, YardBankActivity::class.java)
            startActivity(intent)
        }
    }

    // date picker function
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { datePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                date.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}