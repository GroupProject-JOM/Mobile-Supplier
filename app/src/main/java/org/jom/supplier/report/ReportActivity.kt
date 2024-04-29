package org.jom.supplier.report

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import org.jom.supplier.Methods
import org.jom.supplier.R
import org.jom.supplier.address.EditAddressActivity

class ReportActivity : AppCompatActivity() {
    private lateinit var button: Button

    // get instance of methods class
    val methods = Methods()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // view report
        button = findViewById(R.id.button)
        button.setOnClickListener {
            val url = "https://jom-dev.duckdns.org/supplier/reports/report.html"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}