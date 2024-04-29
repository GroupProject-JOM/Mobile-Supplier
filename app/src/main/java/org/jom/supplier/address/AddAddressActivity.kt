package org.jom.supplier.address

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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

data class AddEstateFormData(
    val estate_name: String,
    val estate_address: String,
    val area: String,
    val estate_location: String,
)

interface AddEstateApi {
    @POST("api/estate")
    fun addEstate(@Body formData: AddEstateFormData): Call<ResponseBody>
}

class AddAddressActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var save: Button

    // status variables for validations
    var estate_name_status = false
    var address_status = false
    var area_status = false

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)

        // initialize inputs and error texts
        val estate_name: EditText = findViewById(R.id.estate_name)
        val address: EditText = findViewById(R.id.address)
        val area: EditText = findViewById(R.id.area)

        val estate_name_error: TextView = findViewById(R.id.estate_name_error)
        val address_error: TextView = findViewById(R.id.address_error)
        val area_error: TextView = findViewById(R.id.area_error)

        val location = "6.8124558 79.895301"

        // Error handling
        fun estate_name_status_func(estate_name: String): Boolean {
            val trimmedEName = estate_name.trim()
            if (trimmedEName.isEmpty()) {
                estate_name_error.text = "Estate name cannot be empty"
                estate_name_status = false
                return false
            } else {
                estate_name_error.text = ""
                estate_name_status = true
                return true
            }
        }

        fun address_status_func(address: String): Boolean {
            val trimmedAddress = address.trim()
            if (trimmedAddress.isEmpty()) {
                address_error.text = "Address cannot be empty"
                address_status = false
                return false
            } else {
                address_error.text = ""
                address_status = true
                return true
            }
        }

        fun area_status_func(area: String): Boolean {
            val trimmedArea = area.trim()
            if (trimmedArea.isEmpty()) {
                area_error.text = "Area cannot be empty"
                area_status = false
                return false
            } else {
                area_error.text = ""
                area_status = true
                return true
            }
        }

        // handle onInput change errors
        val estateNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                estate_name_status_func(s.toString())
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
                address_status_func(s.toString())
            }
        }
        val areaTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                area_status_func(s.toString())
            }
        }

        // call to text watchers
        estate_name.addTextChangedListener(estateNameTextWatcher)
        address.addTextChangedListener(addressTextWatcher)
        area.addTextChangedListener(areaTextWatcher)

        // save changes
        save = findViewById(R.id.save)
        save.setOnClickListener {
            estate_name_status_func(estate_name.text.toString())
            address_status_func(address.text.toString())
            area_status_func(area.text.toString())

            if (estate_name_status && address_status && area_status) {
                // initialize view profile activity
                val intent = Intent(this, ViewAllAddressesActivity::class.java)

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

                val addEstateApi = retrofit.create(AddEstateApi::class.java)

                val formData = AddEstateFormData(
                    estate_name = estate_name.text.toString(),
                    estate_address = address.text.toString(),
                    area = area.text.toString(),
                    estate_location = location,
                )

                if (formData != null) {
                    addEstateApi.addEstate(formData)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onResponse(
                                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    SweetAlertDialog(
                                        this@AddAddressActivity,
                                        SweetAlertDialog.SUCCESS_TYPE
                                    )
                                        .setTitleText("Added!")
                                        .setContentText("Your estate added successfully.")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener {
                                            // after successfully added
                                            startActivity(intent)
                                            finish()
                                        }.show()

                                } else if (response.code() == 202) {
                                    Log.d("TAG", "Cannot add estate")
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
}