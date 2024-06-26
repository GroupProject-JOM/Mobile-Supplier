package org.jom.supplier

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.jom.supplier.supply.NewSupplyActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.time.LocalTime
import android.app.AlertDialog
import android.app.Dialog
import cn.pedant.SweetAlert.SweetAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicBoolean

interface DashboardApi {
    @GET("JOM_war_exploded/collections")
    fun getData(): Call<ResponseBody>
}

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var recycleViewOngoing: RecyclerView
    private lateinit var recycleViewCompleted: RecyclerView
    private lateinit var recycleViewRejected: RecyclerView
    private lateinit var ongoingCollectionsAdapter: CollectionsAdapter
    private lateinit var completedCollectionsAdapter: CollectionsAdapter
    private lateinit var rejectedCollectionsAdapter: CollectionsAdapter
    private lateinit var jwt: String
    private lateinit var dialog: Dialog

    val ongoingCollectionItems = mutableListOf<CollectionItem>()
    val completedCollectionItems = mutableListOf<CollectionItem>()
    val rejectedCollectionItems = mutableListOf<CollectionItem>()

    // get instance of methods class
    val methods = Methods()

    @RequiresApi(Build.VERSION_CODES.O)
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

        val dashboardApi = retrofit.create(DashboardApi::class.java)

        // call get data function to get data from backend
        dashboardApi.getData().enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val income = jsonObject.optDouble("income")

                        val Income: TextView = findViewById(R.id.widget01_value)
                        Income.text = methods.formatAmount(income) + " LKR"


                        val ongoingList = jsonObject.getJSONArray("ongoing")
                        val CompletedList = jsonObject.getJSONArray("past")
                        var count = 0

                        for (i in 0 until ongoingList.length()) {
                            val item = ongoingList.getJSONObject(i)
                            var status = item.getString("status")
                            var oTable = true;

                            val id = item.getInt("id")
                            val date =
                                item.getString("date") + " " + methods.convertTime(item.getString("name"))
                            val amount = methods.formatAmount(item.getDouble("amount"))
                            val method = item.getString("method")

                            if (status == "1") status = "Pending"
                            else if (status == "2") status = "Accepted"
                            else if (status == "3") status = "Ready"
                            else if (status == "4") {
                                status = "Rejected"
                                count++
                                if (!methods.checkDate(date)) oTable = false;
                            }

                            if (oTable) {
                                ongoingCollectionItems.add(
                                    CollectionItem(
                                        id,
                                        date,
                                        amount,
                                        method,
                                        status
                                    )
                                )
                            } else {
                                rejectedCollectionItems.add(
                                    CollectionItem(
                                        id,
                                        date,
                                        amount,
                                        method,
                                        status
                                    )
                                )
                            }
                        }

                        val OngoingCount: TextView = findViewById(R.id.widget02_value)
                        OngoingCount.text = (ongoingList.length() - count).toString()

                        for (i in 0 until CompletedList.length()) {
                            val item = CompletedList.getJSONObject(i)

                            var status = item.getString("status")

                            if (status == "5") status = "Pending"
                            else if (status == "6") status = "Paid"

                            val id = item.getInt("id")
                            val date =
                                item.getString("date")
                            val amount = methods.formatAmount(item.getDouble("final_amount"))
                            val method = item.getString("method")

                            completedCollectionItems.add(
                                CollectionItem(
                                    id,
                                    date,
                                    amount,
                                    method,
                                    status
                                )
                            )

                            // Set up RecyclerView and its adapter after data is fetched
                            runOnUiThread {
                                ongoingCollectionsAdapter.notifyDataSetChanged()
                                rejectedCollectionsAdapter.notifyDataSetChanged()
                                completedCollectionsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                } else if (response.code() == 202) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val income = jsonObject.optDouble("income")

                        val Income: TextView = findViewById(R.id.widget01_value)
                        Income.text = methods.formatAmount(income) + " LKR"

                        var count = 0
                        val OngoingCount: TextView = findViewById(R.id.widget02_value)
                        OngoingCount.text = count.toString()

                        val CompletedList = jsonObject.getJSONArray("past")

                        for (i in 0 until CompletedList.length()) {
                            val item = CompletedList.getJSONObject(i)

                            var status = item.getString("status")

                            if (status == "5") status = "Pending"
                            else if (status == "6") status = "Paid"

                            val id = item.getInt("id")
                            val date =
                                item.getString("date")
                            val amount = methods.formatAmount(item.getDouble("final_amount"))
                            val method = item.getString("method")

                            completedCollectionItems.add(
                                CollectionItem(
                                    id,
                                    date,
                                    amount,
                                    method,
                                    status
                                )
                            )

                            // Set up RecyclerView and its adapter after data is fetched
                            runOnUiThread {
                                completedCollectionsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
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
        setContentView(R.layout.activity_dashboard)

// set collector's name on dashboard
        var collectorName: TextView = findViewById(R.id.collectorName)
        sharedPreferences = getSharedPreferences("login_pref", Context.MODE_PRIVATE)
        collectorName.text = sharedPreferences.getString("name", "Collector")

// set greeting msg
        var greeting: TextView = findViewById(R.id.greeting)
        val currentTime = LocalTime.now()
        greeting.text = methods.getGreetingTime(currentTime)

// nav and status bar color
        window.navigationBarColor = ContextCompat.getColor(this, R.color.lightBodyColor)
        window.statusBarColor = ContextCompat.getColor(this, R.color.lightBodyColor)

// recycle views handle
        recycleViewOngoing = findViewById(R.id.recycleViewOngoing)
        recycleViewCompleted = findViewById(R.id.recycleViewCompleted)
        recycleViewRejected = findViewById(R.id.recycleViewRejected)

        recycleViewOngoing.layoutManager = LinearLayoutManager(this)
        recycleViewCompleted.layoutManager = LinearLayoutManager(this)
        recycleViewRejected.layoutManager = LinearLayoutManager(this)

        ongoingCollectionsAdapter = CollectionsAdapter(ongoingCollectionItems)
        recycleViewOngoing.adapter = ongoingCollectionsAdapter

        completedCollectionsAdapter = CollectionsAdapter(completedCollectionItems)
        recycleViewCompleted.adapter = completedCollectionsAdapter

        rejectedCollectionsAdapter = CollectionsAdapter(rejectedCollectionItems)
        recycleViewRejected.adapter = rejectedCollectionsAdapter

        // mid nav handle
        var ongoing: Button = findViewById(R.id.ongoing)
        var completed: Button = findViewById(R.id.completed)
        var rejected: Button = findViewById(R.id.rejected)
        ongoing.setOnClickListener()
        {
            recycleViewOngoing.visibility = View.VISIBLE
            recycleViewCompleted.visibility = View.GONE
            recycleViewRejected.visibility = View.GONE

            ongoing.setTypeface(null, Typeface.BOLD)
            completed.setTypeface(null, Typeface.NORMAL)
            rejected.setTypeface(null, Typeface.NORMAL)
            ongoing.setTextColor(ContextCompat.getColor(this, R.color.lightSidebarColor))
            completed.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
            rejected.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
        }
        completed.setOnClickListener()
        {
            recycleViewOngoing.visibility = View.GONE
            recycleViewCompleted.visibility = View.VISIBLE
            recycleViewRejected.visibility = View.GONE

            ongoing.setTypeface(null, Typeface.NORMAL)
            completed.setTypeface(null, Typeface.BOLD)
            rejected.setTypeface(null, Typeface.NORMAL)
            ongoing.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
            completed.setTextColor(ContextCompat.getColor(this, R.color.lightSidebarColor))
            rejected.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
        }
        rejected.setOnClickListener()
        {
            recycleViewOngoing.visibility = View.GONE
            recycleViewCompleted.visibility = View.GONE
            recycleViewRejected.visibility = View.VISIBLE

            ongoing.setTypeface(null, Typeface.NORMAL)
            completed.setTypeface(null, Typeface.NORMAL)
            rejected.setTypeface(null, Typeface.BOLD)
            ongoing.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
            completed.setTextColor(ContextCompat.getColor(this, R.color.lightPrimaryColor))
            rejected.setTextColor(ContextCompat.getColor(this, R.color.lightSidebarColor))
        }

        // bottom nav handler
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
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

        //WebSocket
        var payload = methods.getPayload(jwt);

        // assign values to socket operation variables
        val senderId = methods.floatToInt(payload["user"])

        runBlocking {
            val socket = OkHttpClient().newWebSocket(
                Request.Builder()
                    .url("ws://10.0.2.2:8090/JOM_war_exploded/verify-amount/${senderId}")
                    .build(),
                object : WebSocketListener() {
                    private val isConnected = AtomicBoolean(false)

                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        println("WebSocket opened: $response")
                        isConnected.set(true)
                        CoroutineScope(Dispatchers.IO).launch {
                            println("Socket Opened")
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        println("WebSocket onMessage: $text")
                        if (text.isNotEmpty()) {
                            val arr = text.split(":")
                            val amount = arr[0]
                            val id = arr[1].toInt()

                            runOnUiThread {
                                // Initialize the dialog
                                dialog = Dialog(
                                    this@DashboardActivity,
                                    R.style.CustomDialogTheme
                                )  // Apply custom theme (optional)
                                dialog.setContentView(R.layout.popup_layout)

                                // Set title and description (optional)
                                val titleTextView = dialog.findViewById<TextView>(R.id.title)
                                titleTextView.text = "Collector is waiting for your response"
                                val descriptionTextView =
                                    dialog.findViewById<TextView>(R.id.description)
                                descriptionTextView.text =
                                    "Collected coconut amount for supply S/P/${id} is ${amount}"

                                println("Collected coconut amount for supply S/P/${id} is ${amount}")

                                // Get references to buttons
                                val positiveButton =
                                    dialog.findViewById<Button>(R.id.positive_button)
                                val negativeButton =
                                    dialog.findViewById<Button>(R.id.negative_button)

                                // Set button click listeners
                                positiveButton.setOnClickListener {
                                    // Handle positive button click

                                    SweetAlertDialog(
                                        this@DashboardActivity,
                                        SweetAlertDialog.SUCCESS_TYPE
                                    )
                                        .setTitleText("Are you sure?")
                                        .setContentText("You won't be able to revert this!")
                                        .setConfirmText("Accept")
                                        .showCancelButton(true)
                                        .setCancelText("✖")
                                        .setConfirmClickListener { sDialog ->
                                            sDialog.setTitleText("Accepted!")
                                                .setContentText("You have verified that the amount of coconut entered by the collector is correct.")
                                                .setConfirmText("Ok")
                                                .showCancelButton(false)
                                                .setConfirmClickListener {
                                                    // complete collection with actual amount
                                                    webSocket.send("${senderId}:OK:${id}")
                                                    sDialog.dismiss();
                                                }
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                        }.show()


                                    dialog.dismiss()
                                }

                                negativeButton.setOnClickListener {
                                    // Handle negative button click

                                    SweetAlertDialog(
                                        this@DashboardActivity,
                                        SweetAlertDialog.SUCCESS_TYPE
                                    )
                                        .setTitleText("Are you sure?")
                                        .setContentText("You won't be able to revert this!")
                                        .setConfirmText("Deny")
                                        .showCancelButton(true)
                                        .setCancelText("✖")
                                        .setConfirmClickListener { sDialog ->
                                            sDialog.setTitleText("Denied!")
                                                .setContentText("You have denied that the amount of coconut entered by the collector.")
                                                .setConfirmText("Ok")
                                                .showCancelButton(false)
                                                .setConfirmClickListener {
                                                    // complete collection with actual amount
                                                    webSocket.send("${senderId}:Denied:${id}")
                                                    sDialog.dismiss();
                                                }
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                                        }.show()

                                    dialog.dismiss()
                                }

                                // Create custom theme (optional)
                                val customTheme = theme.applyStyle(R.style.CustomDialogTheme, false)

                                // Show the dialog with custom theme (optional)
                                //        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.popup_background))  // Set custom background (optional)
                                dialog.show()
                            }
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        println("WebSocket closed: $code $reason")
                        isConnected.set(false)
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?
                    ) {
                        println("WebSocket error: $t")
                        isConnected.set(false)
                    }
                }
            )
        }
    }
}
