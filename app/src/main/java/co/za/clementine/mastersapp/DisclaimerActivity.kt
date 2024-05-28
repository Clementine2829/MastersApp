package co.za.clementine.mastersapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.security.AppSecurityManager
import kotlin.system.exitProcess

class DisclaimerActivity : AppCompatActivity() {
    private var securityManager: AppSecurityManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimer)

        val appIcon = findViewById<ImageView>(R.id.appIcon)
        val disclaimerText = findViewById<TextView>(R.id.disclaimerText)
        val btnAgree = findViewById<Button>(R.id.btnAgree)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnMoreInfo = findViewById<TextView>(R.id.btnMoreInfo)

        securityManager = AppSecurityManager(this);

        if (!securityManager!!.isUserAuthenticated) {
            securityManager!!.requestAuthentication(this);
        }


        appIcon.setImageResource(R.drawable.ic_launcher_background)  // Replace with your app icon resource
        disclaimerText.text = getString(R.string.disclaimer_text)

        btnAgree.setOnClickListener {
            // Proceed to main activity or the next step
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
            exitProcess(0)
        }


        btnCancel.setOnClickListener {
            // Exit the app or go back
            finish()
        }

        btnMoreInfo.setOnClickListener {
            // Open a web page or activity with more information about T&Cs and Privacy Policy
            val intent = Intent(this, MoreInfoActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        if (!securityManager!!.isUserAuthenticated) {
            securityManager!!.requestAuthentication(this)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        securityManager!!.handleAuthenticationResult(requestCode, resultCode)
//        if (!securityManager!!.isUserAuthenticated) {
//            // Handle the case where authentication failed
//            finish() // or take other appropriate action
//        }
    }
}
