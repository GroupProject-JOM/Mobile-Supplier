package org.jom.supplier.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import retrofit2.http.GET

interface ProfileApi {
    @GET("api/profile")
    fun getData(): Call<ResponseBody>
}

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var edit: Button
    private lateinit var jwt: String

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

        val profileApi = retrofit.create(ProfileApi::class.java)

        // call get data function to get data from backend
        profileApi.getData().enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val user = jsonObject.getJSONObject("user")

                        // initialize text views
                        val first_name: TextView = findViewById(R.id.first_name)
                        val last_name: TextView = findViewById(R.id.last_name)
                        val email: TextView = findViewById(R.id.email)
                        val contact: TextView = findViewById(R.id.contact)
                        val address_1: TextView = findViewById(R.id.address_1)
                        val address_2: TextView = findViewById(R.id.address_2)
                        val address_3: TextView = findViewById(R.id.address_3)

                        // assign values to text views
                        first_name.text = user.getString("first_name")
                        last_name.text = user.getString("last_name")
                        email.text = user.getString("email")
                        contact.text = user.getString("phone")
                        address_1.text = user.getString("add_line_1")
                        address_2.text = user.getString("add_line_2")
                        address_3.text = user.getString("add_line_3")

                        // add data to bundle to send to next intent
                        extras.putString("first_name", user.getString("first_name"))
                        extras.putString("last_name", user.getString("last_name"))
                        extras.putString("phone", user.getString("phone"))
                        extras.putString("add_line_1", user.getString("add_line_1"))
                        extras.putString("add_line_2", user.getString("add_line_2"))
                        extras.putString("add_line_3", user.getString("add_line_3"))
                    }
                } else if (response.code() == 400) {
                    Log.d("TAG", "No user")
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

        // after fetch all data
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        // edit profile
        edit = findViewById(R.id.edit)
        edit.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
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