package org.jom.supplier.address

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import retrofit2.http.GET
import retrofit2.http.Query

interface EstateApi {
    @GET("JOM_war_exploded/estate")
    fun getData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

class ViewAddressActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var edit: Button

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

        val estateApi = retrofit.create(EstateApi::class.java)

        // Retrieve the Intent that started this activity and get the value of the "id" extra
        val intent = intent
        val id = intent.getIntExtra("id", 0)

        // call get data function to get data from backend
        estateApi.getData(id).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val estate = jsonObject.getJSONObject("estate")

                        // initialize text views
                        val estateName: TextView = findViewById(R.id.estate)
                        val address: TextView = findViewById(R.id.address)
                        val area: TextView = findViewById(R.id.area)

                        // assign values to text views
                        estateName.text = estate.getString("estate_name")
                        address.text = estate.getString("estate_address")
                        area.text = estate.getString("area")

                        var location = estate.getString("estate_location")

                        // add data to bundle to send to next intent
                        extras.putString("estate_name", estate.getString("estate_name"))
                        extras.putString("address", estate.getString("estate_address"))
                        extras.putString("area", estate.getString("area"))
                        extras.putString("location", estate.getString("estate_location"))
                    }
                } else if (response.code() == 202) {
                    Log.d("TAG", "No Estare")
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
        setContentView(R.layout.activity_view_address)

        // edit profile
        edit = findViewById(R.id.edit)
        edit.setOnClickListener {
            val intent = Intent(this, EditAddressActivity::class.java)
            intent.putExtras(extras)
            startActivity(intent)
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