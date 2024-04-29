package org.jom.supplier.supply

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
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
import org.jom.supplier.profile.EditProfileApi
import org.jom.supplier.profile.EditProfileFormData
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.profile.ViewProfileActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class NewSupplyFormData(
    val initial_amount: String,
    val payment_method: String,
    val supply_method: String,
)

interface NewSupplyApi {
    @POST("JOM_war_exploded/collection")
    fun newSupply(@Body formData: NewSupplyFormData): Call<ResponseBody>
}

class NewSupplyActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var next: Button
    private lateinit var jwt: String

    // status variables for validations
    var coco_amount_status = false

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_supply)

        // initialize inputs and error texts
        val coco_amount: EditText = findViewById(R.id.coco_amount)
        val pickup: CheckBox = findViewById(R.id.pickup)
        val yard: CheckBox = findViewById(R.id.yard)
        val cash: CheckBox = findViewById(R.id.cash)
        val bank: CheckBox = findViewById(R.id.bank)

        var coco_amount_error: TextView = findViewById(R.id.coco_amount_error)
        var collection_method_error: TextView = findViewById(R.id.collection_method_error)
        var payment_method_error: TextView = findViewById(R.id.payment_method_error)

        // Error handling
        fun coco_amount_status_func(coco_amount: String): Boolean {
            val trimmedAmount = coco_amount.trim()
            if (trimmedAmount.isEmpty()) {
                coco_amount_error.text = "Coconut amount cannot be empty"
                coco_amount_status = false
                return false
            } else if (!methods.checkInt(coco_amount)) {
                coco_amount_error.text = "Coconut amount must be greater than 0"
                coco_amount_status = false
                return false
            } else {
                coco_amount_error.text = ""
                coco_amount_status = true
                return true
            }
        }

        // handle onInput change errors
        val cocoAmountTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                coco_amount_status_func(s.toString())
            }
        }
        val collectionCheckBoxListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (buttonView === pickup) {
                        yard.isChecked = false
                    } else if (buttonView === yard) {
                        pickup.isChecked = false
                    }

                    collection_method_error.text = "";
                } else {
                    collection_method_error.text = "Collection method cannot be empty";
                }
            }
        val paymentCheckBoxListener =
            CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (buttonView === cash) {
                        bank.isChecked = false
                    } else if (buttonView === bank) {
                        cash.isChecked = false
                    }

                    payment_method_error.text = "";
                } else {
                    payment_method_error.text = "Payment method cannot be empty";
                }
            }


        // call to text watchers
        coco_amount.addTextChangedListener(cocoAmountTextWatcher)
        pickup.setOnCheckedChangeListener(collectionCheckBoxListener)
        yard.setOnCheckedChangeListener(collectionCheckBoxListener)
        cash.setOnCheckedChangeListener(paymentCheckBoxListener)
        bank.setOnCheckedChangeListener(paymentCheckBoxListener)

        // next button click to create collection
        next = findViewById(R.id.next)
        next.setOnClickListener {
            var collection = "";
            var money = "";
            lateinit var intent: Intent;

            coco_amount_status_func(coco_amount.text.toString())

            if (pickup.isChecked) {
                collection = "pickup";
                collection_method_error.text = "";
                if (cash.isChecked) {
                    money = "cash";
                    intent = Intent(this, PickupCashActivity::class.java)
                    payment_method_error.text = "";
                } else if (bank.isChecked) {
                    money = "bank";
                    intent = Intent(this, PickupBankActivity::class.java)
                    payment_method_error.text = "";
                } else {
                    payment_method_error.text = "Payment method cannot be empty";
                }
            } else if (yard.isChecked) {
                collection = "yard";
                collection_method_error.text = "";
                if (cash.isChecked) {
                    money = "cash";
                    intent = Intent(this, YardCashActivity::class.java)
                    payment_method_error.text = "";
                } else if (bank.isChecked) {
                    money = "bank";
                    intent = Intent(this, YardBankActivity::class.java)
                    payment_method_error.text = "";
                } else {
                    payment_method_error.text = "Payment method cannot be empty";
                }
            } else {
                collection_method_error.text = "Collection method cannot be empty";
                if (!(cash.isChecked || bank.isChecked))
                    payment_method_error.text = "Payment method cannot be empty";
            }

            // all status true ready send request
            if (coco_amount_status && (cash.isChecked || bank.isChecked) && (pickup.isChecked || yard.isChecked)) {
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

                val newSupplyApi = retrofit.create(NewSupplyApi::class.java)

                val formData = NewSupplyFormData(
                    initial_amount = coco_amount.text.toString(),
                    payment_method = money,
                    supply_method = collection,
                )

                newSupplyApi.newSupply(formData)
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
                                    val id =
                                        jsonObject.optString("id") // Extract message field from JSON
                                    Log.d("TAG", id)

                                    intent.putExtra("id", id)
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
}