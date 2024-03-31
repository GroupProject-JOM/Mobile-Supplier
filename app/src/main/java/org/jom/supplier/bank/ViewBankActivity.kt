package org.jom.supplier.bank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.address.EditAddressActivity
import org.jom.supplier.address.EstateApi
import org.jom.supplier.address.EstateDeleteApi
import org.jom.supplier.address.ViewAllAddressesActivity
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface AccountApi {
    @GET("JOM_war_exploded/account")
    fun getData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

interface AccountDeleteApi {
    @DELETE("JOM_war_exploded/account")
    fun deleteData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

class ViewBankActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var edit: Button
    private lateinit var delete: Button

    // get instance of methods class
    val methods = Methods()

    // get bundle instance for send data for next intent
    var extras = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val accountApi = retrofit.create(AccountApi::class.java)

        // Retrieve the Intent that started this activity and get the value of the "id" extra
        val intent = intent
        val id = intent.getIntExtra("id", 0)

        // call get data function to get data from backend
        accountApi.getData(id).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val estate = jsonObject.getJSONObject("account")

                        // initialize text views
                        val name: TextView = findViewById(R.id.name)
                        val nickName: TextView = findViewById(R.id.nickName)
                        val account_number: TextView = findViewById(R.id.account_number)
                        val bank: TextView = findViewById(R.id.bank)

                        // assign values to text views
                        name.text = estate.getString("name")
                        nickName.text = estate.getString("nickName")
                        account_number.text = estate.getString("account_number")
                        bank.text = estate.getString("bank")

                        // add data to bundle to send to next intent
                        extras.putString("name", estate.getString("name"))
                        extras.putString("nickName", estate.getString("nickName"))
                        extras.putString("account_number", estate.getString("account_number"))
                        extras.putString("bank", estate.getString("bank"))
                        extras.putInt("id", id)
                    }
                } else if (response.code() == 202) {
                    Log.d("TAG", "No Account")
                    Log.d("TAG", response.code().toString())
                } else {
                    Log.d("TAG", "Went wrong")
                    Log.d("TAG", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Handle failure
                Log.d("TAG", "An error occurred: $t")
            }
        })

        // after fetch all data
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bank)

        // edit account
        edit = findViewById(R.id.edit)
        edit.setOnClickListener {
            val intent = Intent(this, EditBankActivity::class.java)
            intent.putExtras(extras)
            startActivity(intent)
        }

        // delete account
        delete = findViewById(R.id.delete)
        delete.setOnClickListener {
            // sweet alert for confirmation
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("You won't be able to revert this!")
                .setConfirmText("Delete")
                .showCancelButton(true)
                .setCancelText("✖")
                .setConfirmClickListener { sDialog ->
                    // Back to view all banks
                    val intent = Intent(this, ViewAllBanksActivity::class.java)

                    val accountDeleteApi = retrofit.create(AccountDeleteApi::class.java)

                    // call delete data function to get data from backend
                    accountDeleteApi.deleteData(id)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    Log.d("TAG", "Delete Account")
                                    Log.d("TAG", response.code().toString())

                                    sDialog.setTitleText("Deleted!")
                                        .setContentText("Your account has been deleted.")
                                        .setConfirmText("Ok")
                                        .showCancelButton(false)
                                        .setConfirmClickListener {
                                            // delete account successfully
                                            startActivity(intent)
                                            finish()
                                        }
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)

                                } else if (response.code() == 202) {
                                    Log.d("TAG", "Unable to Delete Account")
                                    Log.d("TAG", response.code().toString())
                                } else {
                                    Log.d("TAG", "Went wrong")
                                    Log.d("TAG", response.code().toString())
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                // Handle failure
                                Log.d("TAG", "An error occurred: $t")
                            }

                        });

                }.show()
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