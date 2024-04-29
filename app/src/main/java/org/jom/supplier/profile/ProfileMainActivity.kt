package org.jom.supplier.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jom.supplier.DashboardActivity
import org.jom.supplier.MainActivity
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.address.ViewAllAddressesActivity
import org.jom.supplier.bank.ViewAllBanksActivity
import org.jom.supplier.chat.ChatActivity
import org.jom.supplier.report.ReportActivity
import org.jom.supplier.supply.NewSupplyActivity

class ProfileMainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: Button
    private lateinit var address: ConstraintLayout
    private lateinit var payment: ConstraintLayout
    private lateinit var report: ConstraintLayout
    private lateinit var profile: ConstraintLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_main)

        // set supplier's name
        var name: TextView = findViewById(R.id.name)
        sharedPreferences = getSharedPreferences("login_pref", Context.MODE_PRIVATE)
        name.text = buildString {
            append(
                sharedPreferences.getString(
                    "name",
                    "Supplier"
                )
            )
            append(" ")
            append(sharedPreferences.getString("lname", "Supplier"))
        }

        //address widget
        address = findViewById(R.id.address_widget)
        address.setOnClickListener {
            val intent = Intent(this, ViewAllAddressesActivity::class.java)
            startActivity(intent)
        }

        //payment widget
        payment = findViewById(R.id.payment_widget)
        payment.setOnClickListener {
            val intent = Intent(this, ViewAllBanksActivity::class.java)
            startActivity(intent)
        }

        //reports widget
        report = findViewById(R.id.reports_widget)
        report.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        //profile widget
        profile = findViewById(R.id.profile_widget)
        profile.setOnClickListener {
            val intent = Intent(this, ViewProfileActivity::class.java)
            startActivity(intent)
        }

        //back
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { this.onBackPressed() }

        //logout
        logoutButton = findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            // Clearing SharedPreferences
            sharedPreferences.edit().clear().apply()

            val cookieManager = CookieManager.getInstance()
            val cookieName = "jwt"
            val domain = "jom-dev.duckdns.org"
            val path = "/"

            // Expire the cookie by setting its expiry date in the past
            val expiredCookie = "$cookieName=; domain=$domain; path=$path; Max-Age=0"

            // Set the expired cookie to replace the existing one
            cookieManager.setCookie(domain, expiredCookie)

            // redirect to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

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
