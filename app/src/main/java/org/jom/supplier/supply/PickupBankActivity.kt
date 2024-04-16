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
import android.view.View
import android.webkit.CookieManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.CustomArrayAdapter
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.address.AddressApi
import org.jom.supplier.bank.AccountsApi
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

data class PickupBankFormData(
    val collection_id: String,
    val estate_id: String,
    val date: String,
    val time: String,
    val account_id: String,
)

interface PickupBankApi {
    @POST("JOM_war_exploded/pickup")
    fun pickupBank(@Body formData: PickupBankFormData): Call<ResponseBody>
}

class PickupBankActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var next: Button
    private lateinit var jwt: String
    private lateinit var date: EditText
    private lateinit var time: EditText
    private lateinit var bank: Spinner
    private lateinit var estate_location: Spinner
    private lateinit var date_icon: ImageView
    private lateinit var time_icon: ImageView

    private val calendar = Calendar.getInstance()

    // get instance of methods class
    val methods = Methods()

    // status variables for validations
    var date_status = false
    var time_status = false
    var location_status = false
    var bank_status = false

    var selectedBank: Long = 0
    var selectedEstate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickup_bank)

        // bank dropdown spinner
        bank = findViewById(R.id.bank)

        // estate_location dropdown spinner
        estate_location = findViewById(R.id.estate_location)

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

        val addressApi = retrofit.create(AddressApi::class.java)

        // values and ids list
        val customListEstate = mutableListOf<Pair<String, Long>>()

        // Adding the default item
        customListEstate.add(Pair("Select Estate", -1L))

        // call get data function to get data from backend
        addressApi.getData().enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)

                        val addressList = jsonObject.getJSONArray("list")

                        for (i in 0 until addressList.length()) {
                            val item = addressList.getJSONObject(i)

                            customListEstate.add(
                                Pair(
                                    item.getString("estate_name"),
                                    item.getInt("id").toLong()
                                )
                            )

                            // map dropdown content to adapter
                            val adapterEstate = CustomArrayAdapter(
                                this@PickupBankActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                customListEstate.map { it.first },
                                customListEstate.map { it.second }
                            )
                            estate_location.adapter = adapterEstate
                        }

                    }
                } else if (response.code() == 202) {
                    Log.d("TAG", "No Estates")
                    Log.d("TAG", response.code().toString())
                } else if (response.code() == 401) {
                    // unauthorized
                } else {
                    Log.d("TAG", "Went wrong")
                    Log.d("TAG", response.code().toString())
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                // Handle failure
                Log.d("TAG", "An error occurred: $t")
            }
        })

        // values and ids list
        val customListBank = mutableListOf<Pair<String, Long>>()

        // Adding the default item
        customListBank.add(Pair("Select Bank Account", -1L))

        val accountsApi = retrofit.create(AccountsApi::class.java)

        // call get data function to get data from backend
        accountsApi.getData().enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)

                        val addressList = jsonObject.getJSONArray("list")

                        for (i in 0 until addressList.length()) {
                            val item = addressList.getJSONObject(i)

                            customListBank.add(
                                Pair(
                                    item.getString("nickName"),
                                    item.getInt("id").toLong()
                                )
                            )
                        }

                        // map dropdown content to adapter
                        val adapterBank = CustomArrayAdapter(
                            this@PickupBankActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            customListBank.map { it.first },
                            customListBank.map { it.second }
                        )
                        bank.adapter = adapterBank
                    }
                } else if (response.code() == 202) {
                    Log.d("TAG", "No Bank Accounts")
                    Log.d("TAG", response.code().toString())
                } else if (response.code() == 401) {
                    // unauthorized
                } else {
                    Log.d("TAG", "Went wrong")
                    Log.d("TAG", response.code().toString())
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                // Handle failure
                Log.d("TAG", "An error occurred: $t")
            }
        })


        // initialize date time Edit texts
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        date_icon = findViewById(R.id.date_icon)
        time_icon = findViewById(R.id.time_icon)

        // initialize error texts
        var location_error: TextView = findViewById(R.id.location_error)
        var date_error: TextView = findViewById(R.id.date_error)
        var time_error: TextView = findViewById(R.id.time_error)
        var bank_error: TextView = findViewById(R.id.bank_error)

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

        // dropdown trigger
        bank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                bank_error.text = "Bank cannot be empty"
                bank_status = false
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    Toast.makeText(
                        this@PickupBankActivity,
                        "You selected ${
                            adapterView?.getItemAtPosition(position).toString()
                        } \nId $id",
                        Toast.LENGTH_LONG
                    ).show()

                    selectedBank = id
                    bank_error.text = ""
                    bank_status = true
                } else {
                    bank_error.text = "Bank cannot be empty"
                    bank_status = false
                }
            }
        }

        // dropdown trigger
        estate_location.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                location_error.text = "Estate cannot be empty"
                location_status = false
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    Toast.makeText(
                        this@PickupBankActivity,
                        "You selected ${
                            adapterView?.getItemAtPosition(position).toString()
                        } \nId $id",
                        Toast.LENGTH_LONG
                    ).show()

                    selectedEstate = id
                    location_error.text = ""
                    location_status = true
                } else {
                    location_error.text = "Estate cannot be empty"
                    location_status = false
                }
            }
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

            if (date_status && time_status && location_status && bank_status) {
                val pickupBankApi = retrofit.create(PickupBankApi::class.java)

                // Retrieve the Intent that started this activity and get the value of the "id" extra
                var intent = intent
                val id = intent.getStringExtra("id")

                val formData = id?.let { it1 ->
                    PickupBankFormData(
                        collection_id = it1,
                        estate_id = selectedEstate.toString(),
                        date = date.text.toString(),
                        time = time.text.toString(),
                        account_id = selectedBank.toString(),
                    )
                }

                intent = Intent(this, DashboardActivity::class.java)

                if (formData != null) {
                    pickupBankApi.pickupBank(formData)
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