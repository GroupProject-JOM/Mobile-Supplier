package org.jom.supplier.bank

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
import org.jom.supplier.address.AddEstateApi
import org.jom.supplier.address.AddEstateFormData
import org.jom.supplier.address.ViewAllAddressesActivity
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class AddAccountFormData(
    val name: String,
    val nickName: String,
    val account_number: String,
    val bank: String,
)

interface AddAccountApi {
    @POST("api/account")
    fun addAccount(@Body formData: AddAccountFormData): Call<ResponseBody>
}

class AddBankActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var save: Button

    // status variables for validations
    var name_status = false
    var nickName_status = false
    var account_number_status = false
    var bank_status = false

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bank)

        // initialize inputs and error texts
        val name: EditText = findViewById(R.id.name)
        val nickName: EditText = findViewById(R.id.nickName)
        val account_number: EditText = findViewById(R.id.account_number)
        val bank: EditText = findViewById(R.id.bank)

        val name_error: TextView = findViewById(R.id.name_error)
        val nickName_error: TextView = findViewById(R.id.nickName_error)
        val account_number_error: TextView = findViewById(R.id.account_number_error)
        val bank_error: TextView = findViewById(R.id.bank_error)

        // Error handling
        fun name_status_func(name: String): Boolean {
            val trimmedEName = name.trim()
            if (trimmedEName.isEmpty()) {
                name_error.text = "Holder name cannot be empty"
                name_status = false
                return false
            } else {
                name_error.text = ""
                name_status = true
                return true
            }
        }

        fun nickName_status_func(nickName: String): Boolean {
            val trimmedNickName = nickName.trim()
            if (trimmedNickName.isEmpty()) {
                nickName_error.text = "Nickname cannot be empty"
                nickName_status = false
                return false
            } else {
                nickName_error.text = ""
                nickName_status = true
                return true
            }
        }

        fun account_number_status_func(account_number: String): Boolean {
            val trimmedAccountNumber = account_number.trim()
            if (trimmedAccountNumber.isEmpty()) {
                account_number_error.text = "Account number cannot be empty"
                account_number_status = false
                return false
            } else {
                account_number_error.text = ""
                account_number_status = true
                return true
            }
        }

        fun bank_status_func(bank: String): Boolean {
            val trimmedBank = bank.trim()
            if (trimmedBank.isEmpty()) {
                bank_error.text = "Bank cannot be empty"
                bank_status = false
                return false
            } else {
                bank_error.text = ""
                bank_status = true
                return true
            }
        }

        // handle onInput change errors
        val nameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                name_status_func(s.toString())
            }
        }
        val nickNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                nickName_status_func(s.toString())
            }
        }
        val accountNumberTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                account_number_status_func(s.toString())
            }
        }
        val bankTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                bank_status_func(s.toString())
            }
        }

        // call to text watchers
        name.addTextChangedListener(nameTextWatcher)
        nickName.addTextChangedListener(nickNameTextWatcher)
        account_number.addTextChangedListener(accountNumberTextWatcher)
        bank.addTextChangedListener(bankTextWatcher)

        // save changes
        save = findViewById(R.id.save)
        save.setOnClickListener {
            name_status_func(name.text.toString())
            nickName_status_func(nickName.text.toString())
            account_number_status_func(account_number.text.toString())
            bank_status_func(bank.text.toString())

            if (name_status && nickName_status && nickName_status && bank_status) {
                // initialize view profile activity
                val intent = Intent(this, ViewAllBanksActivity::class.java)

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

                val addAccountApi = retrofit.create(AddAccountApi::class.java)

                val formData = AddAccountFormData(
                    name = name.text.toString(),
                    nickName = nickName.text.toString(),
                    account_number = account_number.text.toString(),
                    bank = bank.text.toString()
                )

                if (formData != null) {
                    addAccountApi.addAccount(formData)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onResponse(
                                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    SweetAlertDialog(
                                        this@AddBankActivity,
                                        SweetAlertDialog.SUCCESS_TYPE
                                    )
                                        .setTitleText("Added!")
                                        .setContentText("Your account added successfully.")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener {
                                            // after successfully added
                                            startActivity(intent)
                                            finish()
                                        }.show()

                                } else if (response.code() == 202) {
                                    Log.d("TAG", "Cannot add account")
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