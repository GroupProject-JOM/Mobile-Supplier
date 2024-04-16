package org.jom.supplier.supply

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class YardCashFormData(
    val collection_id: String,
    val date: String,
    val time: String,
)

interface YardCashApi {
    @POST("JOM_war_exploded/yard")
    fun yardCash(@Body formData: YardCashFormData): Call<ResponseBody>
}

class YardCashActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var next: Button
    private lateinit var jwt: String
    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var date_icon: ImageView
    private lateinit var time_icon: ImageView

    // Get calender instance
    private val calendar = Calendar.getInstance()

    // get instance of methods class
    val methods = Methods()

    // status variables for validations
    var date_status = false
    var time_status = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yard_cash)

        // initialize date time Edit texts
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        date_icon = findViewById(R.id.date_icon)
        time_icon = findViewById(R.id.time_icon)

        // initialize error texts
        var date_error: TextView = findViewById(R.id.date_error)
        var time_error: TextView = findViewById(R.id.time_error)

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

        // Error handling
        fun date_status_func(date: String): Boolean {
            val trimmedDate = date.trim()
            if (trimmedDate.isEmpty()) {
                date_error.text = "Date cannot be empty"
                date_status = false
                return false
            } else if (!methods.checkDate(date)) {
                date_error.text = "Date must be in the future"
                date_status = false
                return false
            } else if (methods.checkTwoWeeks(date)) {
                date_error.text = "The date should be within the next two weeks"
                date_status = false
                return false
            } else {
                date_error.text = ""
                date_status = true
                return true
            }
        }

        fun time_status_func(time: String): Boolean {
            val trimmedTime = time.trim()
            if (trimmedTime.isEmpty()) {
                time_error.text = "Time cannot be empty"
                time_status = false
                return false
            } else if (!methods.checkTime(time)) {
                time_error.text = "Time must be between 08:00:AM and 05:00:PM"
                time_status = false
                return false
            } else {
                time_error.text = ""
                time_status = true
                return true
            }
        }

        // handle onInput change errors
        val dateTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                date_status_func(s.toString())
            }
        }
        val timeTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                time_status_func(s.toString())
            }
        }

        // call to text watchers
        date.addTextChangedListener(dateTextWatcher)
        time.addTextChangedListener(timeTextWatcher)

        // next button
        next = findViewById(R.id.next)
        next.setOnClickListener {
            date_status_func(date.text.toString())
            time_status_func(time.text.toString())

            if (date_status && time_status) {
                // get cookie operations
                val cookieManager = CookieManager.getInstance()
                val cookies = methods.getAllCookies(cookieManager)

                // get jwt from cookie
                for (cookie in cookies) {
                    if (cookie.first == "jwt") {
                        jwt = cookie.second
                    }
                }
                val cookiesMap = mapOf(
                    "jwt" to jwt,
                )

                // bind jwt for request
                val client = OkHttpClient.Builder()
                    .addInterceptor(AddCookiesInterceptor(cookiesMap))
                    .build()

                // generate request
                val retrofit = Retrofit.Builder()
                    .baseUrl(methods.getBackendUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val yardCashApi = retrofit.create(YardCashApi::class.java)

                // Retrieve the Intent that started this activity and get the value of the "id" extra
                var intent = intent
                val id = intent.getStringExtra("id")

                val formData = id?.let { it1 ->
                    YardCashFormData(
                        collection_id = it1,
                        date = date.text.toString(),
                        time = time.text.toString(),
                    )
                }

                intent = Intent(this, DashboardActivity::class.java)

                if (formData != null) {
                    yardCashApi.yardCash(formData)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onResponse(
                                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    Log.d("TAG", response.code().toString())
                                    val responseBody = response.body()
                                    responseBody?.let {
                                        val jsonString =
                                            it.string() // Convert response body to JSON string
                                        val jsonObject =
                                            JSONObject(jsonString) // Convert JSON string to JSONObject
                                        val message =
                                            jsonObject.optString("message") // Extract message field from JSON
                                        Log.d("TAG", message)

                                        startActivity(intent)
                                        finish()
                                    }
                                } else if (response.code() == 400) {
                                    // Handle error
                                    Log.d("TAG", response.code().toString())
                                    val responseBody = response.body()
                                    responseBody?.let {
                                        val jsonString =
                                            it.string() // Convert response body to JSON string
                                        val jsonObject =
                                            JSONObject(jsonString) // Convert JSON string to JSONObject
                                        val message =
                                            jsonObject.optString("message") // Extract message field from JSON
                                        Log.d("TAG", message)
                                    }
                                } else if (response.code() == 401) {
                                    Log.d("TAG", "Unauthorized")
                                    Log.d("TAG", response.code().toString())
                                } else {
                                    // Handle error
                                    Log.d("TAG", response.code().toString())
                                    val responseBody = response.body()
                                    responseBody?.let {
                                        val jsonString =
                                            it.string() // Convert response body to JSON string
                                        val jsonObject =
                                            JSONObject(jsonString) // Convert JSON string to JSONObject
                                        val message =
                                            jsonObject.optString("message") // Extract message field from JSON
                                        Log.d("TAG", message)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                // Handle failure
                                Log.d("TAG", "An error occurred: $t")
                            }
                        })
                }
            }
        }

        //back
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { this.onBackPressed() }

        // bottom nav handler
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_new
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)

                    item.setIcon(R.drawable.icon_home)
                    true
                }

                R.id.nav_new -> {
                    val intent = Intent(this, NewSupplyActivity::class.java)
                    startActivity(intent)

                    item.setIcon(R.drawable.icon_new)
                    true
                }

                R.id.nav_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)

                    item.setIcon(R.drawable.icon_chat)
                    true
                }

                R.id.nav_user -> {
                    val intent = Intent(this, ProfileMainActivity::class.java)
                    startActivity(intent)

                    item.setIcon(R.drawable.icon_user)
                    true
                }

                else -> false
            }
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