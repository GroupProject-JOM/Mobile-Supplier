package org.jom.supplier

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.ResponseBody
import org.jom.supplier.profile.ViewProfileActivity
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class signUpFormData(
    val first_name: String,
    val last_name: String,
    val email: String,
    val password: String,
    val phone: String,
    val add_line_1: String,
    val add_line_2: String,
    val add_line_3: String,
)

interface SignupApi {
    @POST("JOM_war_exploded/signup")
    fun signup(@Body formData: signUpFormData): Call<ResponseBody>
}

class SignupActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var submit: Button

    // status variables for validations
    var first_name_status = false
    var last_name_status = false
    var email_status = false
    var password_status = false
    var confirm_password_status = false
    var contact_status = false
    var address_1_status = false
    var address_2_status = false
    var address_3_status = false

    // get instance of methods class
    val methods = Methods()

    // get bundle instance for send data for next intent
    var extras = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // initialize inputs and error texts
        val first_name: EditText = findViewById(R.id.first_name)
        val last_name: EditText = findViewById(R.id.last_name)
        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val confirm_password: EditText = findViewById(R.id.confirm_password)
        val contact: EditText = findViewById(R.id.contact)
        val address_1: EditText = findViewById(R.id.address_1)
        val address_2: EditText = findViewById(R.id.address_2)
        val address_3: EditText = findViewById(R.id.address_3)

        var first_name_error: TextView = findViewById(R.id.first_name_error)
        var last_name_error: TextView = findViewById(R.id.last_name_error)
        var email_error: TextView = findViewById(R.id.email_error)
        var password_error: TextView = findViewById(R.id.password_error)
        var confirm_password_error: TextView = findViewById(R.id.confirm_password_error)
        var contact_error: TextView = findViewById(R.id.contact_error)
        var address_1_error: TextView = findViewById(R.id.address_1_error)
        var address_2_error: TextView = findViewById(R.id.address_2_error)
        var address_3_error: TextView = findViewById(R.id.address_3_error)

        // Error handling
        fun first_name_status_func(fname: String): Boolean {
            val trimmedFName = fname.trim()
            if (trimmedFName.isEmpty()) {
                first_name_error.text = "First name cannot be empty"
                first_name_status = false
                return false
            } else if (!validateName(fname)) {
                first_name_error.text = "Name must contain only letters and ' '"
                last_name_status = false
                return false
            } else {
                first_name_error.text = ""
                first_name_status = true
                return true
            }
        }

        fun last_name_status_func(lname: String): Boolean {
            val trimmedLName = lname.trim()
            if (trimmedLName.isEmpty()) {
                last_name_error.text = "Last name cannot be empty"
                last_name_status = false
                return false
            } else if (!validateName(lname)) {
                last_name_error.text = "Name must contain only letters and ' '"
                last_name_status = false
                return false
            } else {
                last_name_error.text = ""
                last_name_status = true
                return true
            }
        }

        fun email_status_func(email: String): Boolean {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isEmpty()) {
                email_error.text = "Email cannot be empty"
                email_status = false
                return false
            } else if (!validateEmail(email)) {
                email_error.text = "Invalid Email address!"
                email_status = false
                return false
            } else {
                email_error.text = ""
                email_status = true
                return true
            }
        }

        fun password_status_func(password: String): Boolean {
            val trimmedPassword = password.trim()
            if (trimmedPassword.isEmpty()) {
                password_error.text = "Password cannot be empty"
                password_status = false
                return false
            } else if (trimmedPassword.length < 6) {
                password_error.text = "Password length must be greater than or equal to 6"
                password_status = false
                return false
            } else if (!hasNumber(trimmedPassword)) {
                password_error.text = "Password must contain at least one digit"
                password_status = false
                return false
            } else if (!hasLetter(trimmedPassword)) {
                password_error.text = "Password must contain at least one letter"
                password_status = false
                return false
            } else {
                password_error.text = ""
                password_status = true
                return true
            }
        }

        fun confirm_password_status_func(password: String): Boolean {
            val trimmedPassword = password.trim()
            if (trimmedPassword.isEmpty()) {
                confirm_password_error.text = "Password cannot be empty"
                confirm_password_status = false
                return false
            } else if (trimmedPassword.length < 6) {
                confirm_password_error.text = "Password length must be greater than or equal to 6"
                confirm_password_status = false
                return false
            } else if (!hasNumber(trimmedPassword)) {
                confirm_password_error.text = "Password must contain at least one digit"
                confirm_password_status = false
                return false
            } else if (!hasLetter(trimmedPassword)) {
                confirm_password_error.text = "Password must contain at least one letter"
                confirm_password_status = false
                return false
            } else {
                confirm_password_error.text = ""
                confirm_password_status = true
                return true
            }
        }

        fun contact_status_func(contact: String): Boolean {
            val trimmedContact = contact.trim()
            if (trimmedContact.isEmpty()) {
                contact_error.text = "Phone cannot be empty"
                contact_status = false
                return false
            } else if (!validatePhone(contact)) {
                contact_error.text = "Invalid phone number!"
                contact_status = false
                return false
            } else {
                contact_error.text = ""
                contact_status = true
                return true
            }
        }

        fun address_1_status_func(address_1: String): Boolean {
            val trimmedAddress1 = address_1.trim()
            if (trimmedAddress1.isEmpty()) {
                address_1_error.text = "Address line 1 cannot be empty"
                address_1_status = false
                return false
            } else {
                address_1_error.text = ""
                address_1_status = true
                return true
            }
        }

        fun address_2_status_func(street: String): Boolean {
            val trimmedStreet = street.trim()
            if (trimmedStreet.isEmpty()) {
                address_2_error.text = "Street cannot be empty"
                address_2_status = false
                return false
            } else {
                address_2_error.text = ""
                address_2_status = true
                return true
            }
        }

        fun address_3_status_func(city: String): Boolean {
            val trimmedCity = city.trim()
            if (trimmedCity.isEmpty()) {
                address_3_error.text = "City cannot be empty"
                address_3_status = false
                return false
            } else {
                address_3_error.text = ""
                address_3_status = true
                return true
            }
        }

        // handle onInput change errors
        val firstNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                first_name_status_func(s.toString())
            }
        }
        val lastNameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                last_name_status_func(s.toString())
            }
        }
        val emailTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                email_status_func(s.toString())
            }
        }
        val passwordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                password_status_func(s.toString())
            }
        }
        val confirmPasswordTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                confirm_password_status_func(s.toString())
            }
        }
        val contactTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                contact_status_func(s.toString())
            }
        }
        val addressTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_1_status_func(s.toString())
            }
        }
        val streetTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_2_status_func(s.toString())
            }
        }
        val cityTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }

            override fun afterTextChanged(s: Editable?) {
                address_3_status_func(s.toString())
            }
        }

        // call to text watchers
        first_name.addTextChangedListener(firstNameTextWatcher)
        last_name.addTextChangedListener(lastNameTextWatcher)
        email.addTextChangedListener(emailTextWatcher)
        password.addTextChangedListener(passwordTextWatcher)
        confirm_password.addTextChangedListener(confirmPasswordTextWatcher)
        contact.addTextChangedListener(contactTextWatcher)
        address_1.addTextChangedListener(addressTextWatcher)
        address_2.addTextChangedListener(streetTextWatcher)
        address_3.addTextChangedListener(cityTextWatcher)

        // save changes
        submit = findViewById(R.id.submit)
        submit.setOnClickListener {
            first_name_status_func(first_name.text.toString())
            last_name_status_func(last_name.text.toString())
            email_status_func(email.text.toString())
            password_status_func(password.text.toString())
            confirm_password_status_func(confirm_password.text.toString())
            contact_status_func(contact.text.toString())
            address_1_status_func(address_1.text.toString())
            address_2_status_func(address_2.text.toString())
            address_3_status_func(address_3.text.toString())

            var matchStatus = false;
            if (password.text.toString() == confirm_password.text.toString()) matchStatus = true
            else {
                password_error.text = "Password and confirm passwords are not matched"
                confirm_password_error.text = "Password and confirm passwords are not matched"
            }

            if (first_name_status && last_name_status && email_status && password_status && confirm_password_status && matchStatus && contact_status && address_1_status && address_2_status && address_3_status) {
                // initialize view profile activity
                val intent = Intent(this, VerificationActivity::class.java)

                val retrofit = Retrofit.Builder().baseUrl(methods.getBackendUrl())
                    .addConverterFactory(GsonConverterFactory.create()).build()

                val signupApi = retrofit.create(SignupApi::class.java)

                val formData = signUpFormData(
                    first_name = first_name.text.toString(),
                    last_name = last_name.text.toString(),
                    email = email.text.toString(),
                    password = password.text.toString(),
                    phone = contact.text.toString(),
                    add_line_1 = address_1.text.toString(),
                    add_line_2 = address_2.text.toString(),
                    add_line_3 = address_3.text.toString(),
                )

                signupApi.signup(formData).enqueue(object : retrofit2.Callback<ResponseBody> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>
                    ) {
                        if (response.code() == 200) {
                            val id = ""
                            val sId = ""
                            val email = ""

                            val responseBody = response.body()
                            responseBody?.let {
                                val jsonString =
                                    it.string() // Convert response body to JSON string
                                val jsonObject = JSONObject(jsonString)
                                val message = jsonObject.optString("message")
                                val id = jsonObject.optString("id")
                                val sId = jsonObject.optString("sId")
                                val email = jsonObject.optString("email")
                                Log.d("TAG", message)

                            }

                            // send to bundle data verification page
                            extras.putString("id", id)
                            extras.putString("sId", sId)
                            extras.putString("email", email)
                            intent.putExtras(extras)
                            startActivity(intent)
                        } else if (response.code() == 409) {
                            email_error.text = "This email is already used!"
                            email.requestFocus()
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

            //back
            backButton = findViewById(R.id.back_button)
            backButton.setOnClickListener { this.onBackPressed() }
        }
    }

    fun validateName(name: String): Boolean {
        val nameRegex = Regex("^[a-zA-Z ]{2,30}$")
        return nameRegex.matches(name)
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("""^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$""")
        return emailRegex.matches(email)
    }

    fun validatePhone(number: String): Boolean {
        val phoneRegex = Regex("""^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$""")
        return phoneRegex.matches(number)
    }

    fun hasLetter(str: String): Boolean {
        val regex = Regex("[a-zA-Z]")
        return regex.containsMatchIn(str)
    }

    fun hasNumber(str: String): Boolean {
        val regex = Regex("\\d")
        return regex.containsMatchIn(str)
    }
}