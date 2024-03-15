package org.jom.supplier

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var logo: ImageView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("login_pref", Context.MODE_PRIVATE)

        // Check if the user is already logged in
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // If the user is logged in, start the main activity
            startActivity(Intent(this, DashboardActivity::class.java))
            finish() // Finish the main activity
        }else {

            // redirect login page to when logo is clicked
            logo = findViewById(R.id.JomLogo)
            logo.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // nav and status bar colors
            window.navigationBarColor = ContextCompat.getColor(this, R.color.lightBodyColor)
            window.statusBarColor = ContextCompat.getColor(this, R.color.darkBoxShadow)

            // auto redirect login page after 3s
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }, 3000.toLong())

        }

    }
}