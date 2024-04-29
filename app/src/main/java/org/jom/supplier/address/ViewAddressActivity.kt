package org.jom.supplier.address

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query

interface EstateApi {
    @GET("api/estate")
    fun getData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

interface EstateDeleteApi {
    @DELETE("api/estate")
    fun deleteData(
        @Query("id") id: Int,
    ): Call<ResponseBody>
}

class ViewAddressActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var jwt: String
    private lateinit var edit: Button
    private lateinit var delete: Button
    private lateinit var location: String

    private lateinit var mapView: MapView
    private lateinit var locationManager: LocationManager

    private val REQUEST_LOCATION_CODE = 100

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

                        location = estate.getString("estate_location")

                        // add data to bundle to send to next intent
                        extras.putString("estate_name", estate.getString("estate_name"))
                        extras.putString("address", estate.getString("estate_address"))
                        extras.putString("area", estate.getString("area"))
                        extras.putString("location", estate.getString("estate_location"))
                        extras.putInt("id", id)
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


        // edit address
        edit = findViewById(R.id.edit)
        edit.setOnClickListener {
            val intent = Intent(this, EditAddressActivity::class.java)
            intent.putExtras(extras)
            startActivity(intent)
        }

        // delete address
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
                    // Back to view all addresses
                    val intent = Intent(this, ViewAllAddressesActivity::class.java)

                    val estateDeleteApi = retrofit.create(EstateDeleteApi::class.java)

                    // call delete data function to get data from backend
                    estateDeleteApi.deleteData(id)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    Log.d("TAG", "Delete Estate")
                                    Log.d("TAG", response.code().toString())

                                    sDialog.setTitleText("Deleted!")
                                        .setContentText("Your address has been deleted.")
                                        .setConfirmText("Ok")
                                        .showCancelButton(false)
                                        .setConfirmClickListener {
                                            // delete address successfully
                                            startActivity(intent)
                                            finish()
                                        }
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)

                                } else if (response.code() == 202) {
                                    Log.d("TAG", "Unable to Delete Estate")
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