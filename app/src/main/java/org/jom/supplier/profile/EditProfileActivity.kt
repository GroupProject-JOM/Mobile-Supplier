package org.jom.supplier.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.PUT

data class EditProfileFormData(
    val first_name: String,
    val last_name: String,
    val phone: String,
    val add_line_1: String,
    val add_line_2: String,
    val add_line_3: String,
)

interface EditProfileApi {
    @PUT("api/profile")
    fun editProfile(@Body formData: EditProfileFormData): Call<ResponseBody>
}

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var save: Button
    private lateinit var jwt: String

    // status variables for validations
    var first_name_status = false
    var last_name_status = false
    var contact_status = false
    var address_1_status = false
    var address_2_status = false
    var address_3_status = false

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // initialize inputs and error texts
        val first_name: EditText = findViewById(R.id.first_name)
        val last_name: EditText = findViewById(R.id.last_name)
        val contact: EditText = findViewById(R.id.contact)
        val address_1: EditText = findViewById(R.id.address_1)
        val address_2: EditText = findViewById(R.id.address_2)
        val address_3: EditText = findViewById(R.id.address_3)

        var fname_error: TextView = findViewById(R.id.fname_error)
        var lname_error: TextView = findViewById(R.id.lname_error)
        var phone_error: TextView = findViewById(R.id.phone_error)
        var address_error: TextView = findViewById(R.id.address_error)
        var street_error: TextView = findViewById(R.id.street_error)
        var city_error: TextView = findViewById(R.id.city_error)

        // get intent instance and bundle
        var i = intent
        var extras = i.extras!!

        // get put data from bundle and assign to Edit texts
        first_name.setText(extras.getString("first_name"))
        last_name.setText(extras.getString("last_name"))
        contact.setText(extras.getString("phone"))
        address_1.setText(extras.getString("add_line_1"))
        address_2.setText(extras.getString("add_line_2"))
        address_3.setText(extras.getString("add_line_3"))

        // Error handling
        fun first_name_status_func(fname: String): Boolean {
            val trimmedFName = fname.trim()
            if (trimmedFName.isEmpty()) {
                fname_error.text = "First name cannot be empty"
                first_name_status = false
                return false
            } else if (!validateName(fname)) {
                lname_error.text = "Name must contain only letters and ' '"
                last_name_status = false
                return false
            } else {
                fname_error.text = ""
                first_name_status = true
                return true
            }
        }

        fun last_name_status_func(lname: String): Boolean {
            val trimmedLName = lname.trim()
            if (trimmedLName.isEmpty()) {
                lname_error.text = "Last name cannot be empty"
                last_name_status = false
                return false
            } else if (!validateName(lname)) {
                lname_error.text = "Name must contain only letters and ' '"
                last_name_status = false
                return false
            } else {
                lname_error.text = ""
                last_name_status = true
                return true
            }
        }

        fun contact_status_func(contact: String): Boolean {
            val trimmedContact = contact.trim()
            if (trimmedContact.isEmpty()) {
                phone_error.text = "Phone cannot be empty"
                contact_status = false
                return false
            } else if (!validatePhone(contact)) {
                phone_error.text = "Invalid phone number!"
                contact_status = false
                return false
            } else {
                phone_error.text = ""
                contact_status = true
                return true
            }
        }

        fun address_1_status_func(address_1: String): Boolean {
            val trimmedAddress1 = address_1.trim()
            if (trimmedAddress1.isEmpty()) {
                address_error.text = "Address line 1 cannot be empty"
                address_1_status = false
                return false
            } else {
                address_error.text = ""
                address_1_status = true
                return true
            }
        }

        fun address_2_status_func(street: String): Boolean {
            val trimmedStreet = street.trim()
            if (trimmedStreet.isEmpty()) {
                street_error.text = "Street cannot be empty"
                address_2_status = false
                return false
            } else {
                street_error.text = ""
                address_2_status = true
                return true
            }
        }

        fun address_3_status_func(city: String): Boolean {
            val trimmedCity = city.trim()
            if (trimmedCity.isEmpty()) {
                city_error.text = "City cannot be empty"
                address_3_status = false
                return false
            } else {
                city_error.text = ""
                address_3_status = true
                return true
            }
        }

        // handle onInput change errors
        val firstNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                first_name_status_func(s.toString())
            }
        }
        val lastNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                last_name_status_func(s.toString())
            }
        }
        val contactTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                contact_status_func(s.toString())
            }
        }
        val addressTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_1_status_func(s.toString())
            }
        }
        val streetTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_2_status_func(s.toString())
            }
        }
        val cityTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_3_status_func(s.toString())
            }
        }

        // call to text watchers
        first_name.addTextChangedListener(firstNameTextWatcher)
        last_name.addTextChangedListener(lastNameTextWatcher)
        contact.addTextChangedListener(contactTextWatcher)
        address_1.addTextChangedListener(addressTextWatcher)
        address_2.addTextChangedListener(streetTextWatcher)
        address_3.addTextChangedListener(cityTextWatcher)

        // save changes
        save = findViewById(R.id.save)
        save.setOnClickListener {

            first_name_status_func(first_name.text.toString())
            last_name_status_func(last_name.text.toString())
            contact_status_func(contact.text.toString())
            address_1_status_func(address_1.text.toString())
            address_2_status_func(address_2.text.toString())
            address_3_status_func(address_3.text.toString())

            if (first_name_status && last_name_status && contact_status && address_1_status && address_2_status && address_3_status) {
                // initialize view profile activity
                val intent = Intent(this, ViewProfileActivity::class.java)

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

                val editProfileApi = retrofit.create(EditProfileApi::class.java)

                val formData = EditProfileFormData(
                    first_name = first_name.text.toString(),
                    last_name = last_name.text.toString(),
                    phone = contact.text.toString(),
                    add_line_1 = address_1.text.toString(),
                    add_line_2 = address_2.text.toString(),
                    add_line_3 = address_3.text.toString()
                )

                editProfileApi.editProfile(formData)
                    .enqueue(object : retrofit2.Callback<ResponseBody> {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResponse(
                            call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                        ) {
                            if (response.code() == 200) {

                                // change shared preferences
                                sharedPreferences =
                                    getSharedPreferences("login_pref", Context.MODE_PRIVATE)
                                sharedPreferences.edit()
                                    .putString("name", first_name.text.toString())
                                    .apply()
                                sharedPreferences.edit()
                                    .putString("lname", last_name.text.toString())
                                    .apply()

                                SweetAlertDialog(
                                    this@EditProfileActivity,
                                    SweetAlertDialog.SUCCESS_TYPE
                                )
                                    .setTitleText("Updated!")
                                    .setContentText("Your profile has been successfully updated.")
                                    .setConfirmText("Ok")
                                    .setConfirmClickListener {
                                        // after successful update
                                        startActivity(intent)
                                        finish()
                                    }.show()

                            } else if (response.code() == 202) {
                                Log.d("TAG", "Cannot update profile")
                                Log.d("TAG", response.code().toString())
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

        //back
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { this.onBackPressed() }

        // bottom nav handler
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_user
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

    fun validateName(name: String): Boolean {
        val nameRegex = Regex("^[a-zA-Z ]{2,30}$")
        return nameRegex.matches(name)
    }

    fun validatePhone(number: String): Boolean {
        val phoneRegex = Regex("""^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$""")
        return phoneRegex.matches(number)
    }
}