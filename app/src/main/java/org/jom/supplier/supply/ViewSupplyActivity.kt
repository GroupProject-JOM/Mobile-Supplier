package org.jom.supplier.supply

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.jom.supplier.AddCookiesInterceptor
import org.jom.supplier.DashboardActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.profile.ProfileMainActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicBoolean

interface SupplyApi {
    @GET("JOM_war_exploded/supply-request")
    fun getData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

class ViewSupplyActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var location: String
    private lateinit var dialog: Dialog

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private val REQUEST_LOCATION_CODE = 100

    // get instance of methods class
    val methods = Methods()

    // get bundle instance for send data for next intent
    var extras = Bundle()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
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

        val supplyApi = retrofit.create(SupplyApi::class.java)

        // Retrieve the Intent that started this activity and get the value of the "id" extra
        val intent = intent
        val id = intent.getIntExtra("id", 0)

        // call get data function to get data from backend
        supplyApi.getData(id).enqueue(object : retrofit2.Callback<ResponseBody> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
            ) {
                if (response.code() == 200) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val request = jsonObject.getJSONObject("request")

                        Log.d("TAG", request.toString())

                        // initialize text views
                        val page_title: TextView = findViewById(R.id.page_title)
                        val supply: TextView = findViewById(R.id.supply)
                        val estate_name_text: TextView = findViewById(R.id.estate_name_text)
                        val estate_name: TextView = findViewById(R.id.estate_name)
                        val estate_address_text: TextView = findViewById(R.id.estate_address_text)
                        val estate_address: TextView = findViewById(R.id.estate_address)
                        val estate_area_text: TextView = findViewById(R.id.estate_area_text)
                        val estate_area: TextView = findViewById(R.id.estate_area)
                        val payment: TextView = findViewById(R.id.payment)
                        val account_name_text: TextView = findViewById(R.id.account_name_text)
                        val account_name: TextView = findViewById(R.id.account_name)
                        val account_number_text: TextView = findViewById(R.id.account_number_text)
                        val account_number: TextView = findViewById(R.id.account_number)
                        val bank_text: TextView = findViewById(R.id.bank_text)
                        val bank: TextView = findViewById(R.id.bank)
                        val pickup_date_text: TextView = findViewById(R.id.pickup_date_text)
                        val pickup_date: TextView = findViewById(R.id.pickup_date)
                        val pickup_time_text: TextView = findViewById(R.id.pickup_time_text)
                        val pickup_time: TextView = findViewById(R.id.pickup_time)
                        val collector_name_text: TextView = findViewById(R.id.collector_name_text)
                        val collector_name: TextView = findViewById(R.id.collector_name)
                        val collector_phone_text: TextView = findViewById(R.id.collector_phone_text)
                        val collector_phone: TextView = findViewById(R.id.collector_phone)
                        val coconut_amount: TextView = findViewById(R.id.coconut_amount)
                        val final_amount_text: TextView = findViewById(R.id.final_amount_text)
                        val final_amount: TextView = findViewById(R.id.final_amount)
                        val status: TextView = findViewById(R.id.status)
                        val value_text: TextView = findViewById(R.id.value_text)
                        val value: TextView = findViewById(R.id.value)
                        val reject: TextView = findViewById(R.id.reject)


                        val edit: Button = findViewById(R.id.edit)
                        val delete: Button = findViewById(R.id.delete)

                        // assign values to text views
                        page_title.text = "Collection ID S/${
                            request.getString("method").capitalize()[0]
                        }/${request.getString("id")}"

                        if (request.getString("method") == "pickup") {
                            supply.text = "Pickup from estate"
                            pickup_date_text.text = "Pickup Date"
                            pickup_time_text.text = "Pickup Time"
                            estate_name.text = request.getString("ename")
                            estate_address.text = request.getString("address")
                            estate_area.text = request.getString("area")
                            location = request.getString("location")

                            mapView.visibility = View.VISIBLE
                            estate_name.visibility = View.VISIBLE
                            estate_address.visibility = View.VISIBLE
                            estate_area.visibility = View.VISIBLE

                            estate_name_text.visibility = View.VISIBLE
                            estate_address_text.visibility = View.VISIBLE
                            estate_area_text.visibility = View.VISIBLE

                            if (request.getString("status").toInt() != 1) {
                                collector_name.text =
                                    "${request.getString("c_fName")} ${request.getString("c_lName")}"
                                collector_phone.text = request.getString("c_phone")

                                collector_name_text.visibility = View.VISIBLE
                                collector_phone_text.visibility = View.VISIBLE

                                collector_name.visibility = View.VISIBLE
                                collector_phone.visibility = View.VISIBLE
                            }
                        } else if (request.getString("method") == "yard") {
                            supply.text = "Delivered to Yard"
                            pickup_date_text.text = "Delivery Date"
                            pickup_time_text.text = "Delivery Time"
                        }

                        pickup_date.text = request.getString("date")
                        pickup_time.text = methods.convertTime(request.getString("time"))

                        if (request.getString("payment_method") == "cash") {
                            payment.text = "Cash on Pickup"
                        } else if (request.getString("payment_method") == "bank") {
                            payment.text = "Bank Transfer"
                            account_name.text = request.getString("h_name")
                            account_number.text = request.getString("account")
                            bank.text = request.getString("bank")

                            account_name.visibility = View.VISIBLE
                            account_number.visibility = View.VISIBLE
                            bank.visibility = View.VISIBLE

                            account_name_text.visibility = View.VISIBLE
                            account_number_text.visibility = View.VISIBLE
                            bank_text.visibility = View.VISIBLE
                        }

                        if (request.getString("status").toInt() in 1..4) {
                            if (request.getString("status").toInt() == 1) {
                                status.text = "Pending Approval"
                            } else if (request.getString("status").toInt() == 2) {
                                status.text = "Accepted"
                            } else if (request.getString("status").toInt() == 3) {
                                status.text = "Ready to Pickup"
                            } else if (request.getString("status").toInt() == 4) {
                                status.text = "Rejected"
                                reject.text = "Reason: ${request.getString("reason")}"

                                reject.visibility = View.VISIBLE
                                edit.visibility = View.GONE
                                delete.visibility = View.GONE
                            }

                            coconut_amount.text =
                                methods.formatAmount(request.getString("amount").toDouble())

                            val selectedDate = LocalDateTime.parse(
                                "${request.getString("date")}T${request.getString("time")}",
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            )
                            val now = LocalDateTime.now().plusDays(1)
                            val weekAgo = LocalDateTime.now().minusWeeks(1)
                            val dDate = selectedDate.plusDays(7)

                            if (selectedDate <= now || request.getString("final_amount")
                                    .toInt() != 0
                            ) {
                                edit.visibility = View.GONE
                                delete.visibility = View.GONE

                                reject.visibility = View.VISIBLE
                                reject.text =
                                    "If you want to delete this, you can do so after " + dDate
                            }

                            if (selectedDate < weekAgo) {
                                if (request.getString("status").toInt() == 4) {
                                    delete.visibility = View.GONE
                                }
                            }
                        } else if (request.getString("status").toInt() in 5..6) {
                            if (request.getString("status").toInt() == 5) {
                                status.text = "Pending Payment"
                            } else {
                                status.text = "Paid"
                            }
                            coconut_amount.text =
                                methods.formatAmount(request.getString("amount").toDouble())
                            final_amount.text =
                                methods.formatAmount(request.getString("final_amount").toDouble())
                            value.text =
                                methods.formatAmount(request.getString("value").toDouble()) + " LKR"

                            final_amount.visibility = View.VISIBLE
                            final_amount_text.visibility = View.VISIBLE
                            value.visibility = View.VISIBLE
                            value_text.visibility = View.VISIBLE
                            edit.visibility = View.GONE
                            delete.visibility = View.GONE
                        }
                    }
                } else if (response.code() == 202) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val jsonString = it.string() // Convert response body to JSON string
                        val jsonObject = JSONObject(jsonString)
                        val collection = jsonObject.optString("request")

                        println(collection)
                        Log.d("TAG", collection)
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
        setContentView(R.layout.activity_view_supply)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState) // Important for lifecycle management

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check and request location permission (important for Android 6.0+)
        if (checkLocationPermission()) {
            // Location permission granted, configure map to show user location
            configureMapView()
        } else {
            requestLocationPermission()
        }

        //back
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { this.onBackPressed() }

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

                            if (id == this@ViewSupplyActivity.intent.getIntExtra("id", 0)) {

                                runOnUiThread {
                                    // Initialize the dialog
                                    dialog = Dialog(
                                        this@ViewSupplyActivity,
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
                                            this@ViewSupplyActivity,
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
                                            this@ViewSupplyActivity,
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
                                    val customTheme =
                                        theme.applyStyle(R.style.CustomDialogTheme, false)

                                    // Show the dialog with custom theme (optional)
                                    //        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.popup_background))  // Set custom background (optional)
                                    dialog.show()
                                }
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

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun checkLocationPermission(): Boolean {
        // Check for permissions (adjust based on your target SDK version)
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        // Request permissions (adjust based on your target SDK version)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (::location.isInitialized && location.isNotBlank()) {
                configureMapView()
            }
        }
    }

    private fun configureMapView() {
        if (::location.isInitialized && location.isNotBlank()) {
            mapView.getMapAsync { map ->
                // Split the location string into latitude and longitude
                val locationSplit = location.split(" ")
                val latitude = locationSplit[0].toDouble()
                val longitude = locationSplit[1].toDouble()

                // Create LatLng object for the location
                val collectionLocation = LatLng(latitude, longitude)

                // Move camera to the collection location
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        collectionLocation,
                        15f
                    )
                ) // Adjust zoom level as needed

                // Add a marker at the collection location
                map.addMarker(
                    MarkerOptions().position(collectionLocation).title("Collection Location")
                )
            }
        }
    }

}