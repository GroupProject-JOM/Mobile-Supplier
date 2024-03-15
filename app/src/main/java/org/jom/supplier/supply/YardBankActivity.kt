package org.jom.supplier.supply

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.ImageView
import org.jom.supplier.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.jom.supplier.CustomArrayAdapter

class YardBankActivity : AppCompatActivity() {

    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var bank: Spinner
    private lateinit var date_icon: ImageView
    private lateinit var time_icon: ImageView
    private lateinit var next: Button

    // Get calender instance
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yard_bank)

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

        // bank dropdown spinner
        bank = findViewById(R.id.bank)

        // values and ids list
        val customList = listOf(
            Pair("Select Bank Account", -1L),  // Default item with custom ID -1
            Pair("Bank 1", 1L),
            Pair("Bank 2", 2L),
            Pair("Bank 3", 3L),
            Pair("Bank 4", 4L)
        )

        // map dropdown content to adapter
        val adapter = CustomArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            customList.map { it.first },
            customList.map { it.second }
        )
        bank.adapter = adapter

        // dropdown trigger
        bank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                    this@YardBankActivity,
                    "You selected ${adapterView?.getItemAtPosition(position).toString()} \nId $id",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // net button
        next = findViewById(R.id.next)
        next.setOnClickListener{
            val intent = Intent(this, YardCashActivity::class.java)
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