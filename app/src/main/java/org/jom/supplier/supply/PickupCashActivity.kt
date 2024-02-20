package org.jom.supplier.supply

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import org.jom.supplier.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PickupCashActivity : AppCompatActivity() {

    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var date_icon: ImageView
    private lateinit var time_icon: ImageView

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_cash)

        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        date_icon = findViewById(R.id.date_icon)
        time_icon = findViewById(R.id.time_icon)

        date.isFocusable = false
        time.isFocusable = false

        date.setOnClickListener {
            date_icon.callOnClick()
        }

        date_icon.setOnClickListener {
            showDatePicker()
        }

        time.setOnClickListener {
            time_icon.callOnClick()
        }

        time_icon.setOnClickListener {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                time.setText(SimpleDateFormat("HH:mm").format(calendar.time))
            }
            TimePickerDialog(
                this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()

        }
    }

    val sDate = Locale.getDefault()
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { datePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", sDate)
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