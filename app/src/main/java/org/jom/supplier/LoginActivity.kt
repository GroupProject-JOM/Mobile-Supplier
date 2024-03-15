package org.jom.supplier

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class signInFormData(
    val username: String,
    val password: String,
)

interface SigninApi {
    @POST("JOM_war_exploded/signin")
    fun signin(@Body formData: signInFormData): Call<ResponseBody>
}

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    lateinit private var loginBtn: Button

    // status variables for validations
    var username_status = false
    var password_status = false

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("login_pref", Context.MODE_PRIVATE)

        // Check if the user is already logged in
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            // If the user is logged in, start the main activity
            startActivity(Intent(this, DashboardActivity::class.java))
            finish() // Finish the login activity
        }

        // initialize dashboard activity
        val intent = Intent(this, DashboardActivity::class.java)

        // set nav and status bar colors
        window.navigationBarColor = ContextCompat.getColor(this, R.color.lightBodyColor)
        window.statusBarColor = ContextCompat.getColor(this, R.color.loadingStatus)

        // set username and password input values to variables
        val username: EditText = findViewById(R.id.username_input)
        val password: EditText = findViewById(R.id.password_input)

        var usernameError: TextView = findViewById(R.id.usernameError)
        var passwordError: TextView = findViewById(R.id.passwordError)

        // Error handling
        fun username_status_func(username: String): Boolean {
            val trimmedUsername = username.trim()
            if (trimmedUsername.isEmpty()) {
                usernameError.text = "Username cannot be empty"
                username_status = false
                return false
            } else {
                usernameError.text = ""
                username_status = true
                return true
            }
        }

        // Error handling
        fun password_status_func(password: String): Boolean {
            val trimmedPassword = password.trim()
            if (trimmedPassword.isEmpty()) {
                passwordError.text = "Password cannot be empty"
                password_status = false
                return false
            } else {
                passwordError.text = ""
                password_status = true
                return true
            }
        }

        // handle onInput change errors
        val usernameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                username_status_func(s.toString())
            }
        }
        val passwordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                password_status_func(s.toString())
            }
        }

        username.addTextChangedListener(usernameTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)

        // initialize login button
        loginBtn = findViewById(R.id.login)

        loginBtn.setOnClickListener {
            if (!password_status_func(password.text.toString())) {
                password.requestFocus()
            }
            if (!username_status_func(username.text.toString())) {
                username.requestFocus()
            }

            if (username_status && password_status) {
                val retrofit = Retrofit.Builder().baseUrl(methods.getBackendUrl())
                    .addConverterFactory(GsonConverterFactory.create()).build()

                val signinApi = retrofit.create(SigninApi::class.java)

                val formData = signInFormData(
                    username = username.text.toString(),
                    password = password.text.toString(),
                )

                signinApi.signin(formData).enqueue(object : retrofit2.Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                    ) {
                        if (response.code() == 200) {

                            val responseBody = response.body()
                            responseBody?.let {
                                val jsonString =
                                    it.string() // Convert response body to JSON string
                                val jsonObject = JSONObject(jsonString)
                                val message = jsonObject.optString("message")
                                val jwt = jsonObject.optString("jwt")
                                Log.d("TAG", message)

                                val cookieManager = CookieManager.getInstance()
                                val cookieName = "jwt"
                                val cookieValue = jwt
                                val domain = "10.0.2.2"
                                val path = "/"

                                // Create a cookie string
                                val cookieString =
                                    "$cookieName=$cookieValue; domain=$domain; path=$path"

                                // Add the cookie to the CookieManager
                                cookieManager.setCookie(domain, cookieString)

                                var payload = methods.getPayload(jwt);
                                val name = payload["name"]
                                val lname = payload["lname"]

                                // check if not a collector
                                val role = payload["page"]
                                if (role != "supplier") {
                                    val toastMessage =
                                        "You are not a supplier. you can not login to this portal."
                                    Toast.makeText(
                                        applicationContext,
                                        toastMessage,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    username.text.clear()
                                    password.text.clear()
                                    return
                                }

                                // After successful login, store the login status in SharedPreferences
                                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                sharedPreferences.edit().putString("name", name.toString())
                                    .apply()
                                sharedPreferences.edit().putString("lname", lname.toString())
                                    .apply()
                            }

                            startActivity(intent)
                            finish()
                        } else if (response.code() == 202) {
                            passwordError.text = "Invalid Password!"
                            password.requestFocus()
                        } else if (response.code() == 401) {
                            usernameError.text = "Invalid Username!"
                            username.requestFocus()
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
    }
}
