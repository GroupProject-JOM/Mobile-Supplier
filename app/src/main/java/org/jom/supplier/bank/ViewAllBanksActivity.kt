package org.jom.supplier.bank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.address.AddAddressActivity
import org.jom.supplier.address.AddressAdapter
import org.jom.supplier.address.AddressApi
import org.jom.supplier.address.AddressItem
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface AccountsApi {
    @GET("api/accounts")
    fun getData(): Call<ResponseBody>
}

class ViewAllBanksActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var recycleViewAccounts: RecyclerView
    private lateinit var accountsAdapter: AccountAdapter
    private lateinit var jwt: String
    private lateinit var add: Button

    val accountsItems = mutableListOf<AccountItem>()

    // get instance of methods class
    val methods = Methods()

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

                            val id = item.getInt("id")
                            val nickName = item.getString("nickName")
                            val accountNumber = item.getString("account_number")

                            accountsItems.add(
                                AccountItem(
                                    id,
                                    accountNumber,
                                    nickName
                                )
                            );

                            // Set up RecyclerView and its adapter after data is fetched
                            runOnUiThread {
                                accountsAdapter.notifyDataSetChanged()
                            }
                        }

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

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_banks)

        // recycle views handle
        recycleViewAccounts = findViewById(R.id.accounts_container)
        recycleViewAccounts.layoutManager = LinearLayoutManager(this)

        accountsAdapter = AccountAdapter(accountsItems)
        recycleViewAccounts.adapter = accountsAdapter

        // add address
        add = findViewById(R.id.add)
        add.setOnClickListener {
            val intent = Intent(this, AddBankActivity::class.java)
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