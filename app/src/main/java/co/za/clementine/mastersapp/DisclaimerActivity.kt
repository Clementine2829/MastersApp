package co.za.clementine.mastersapp

import android.app.AlertDialog
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
        supportActionBar?.hide()

        val appIcon = findViewById<ImageView>(R.id.appIcon)
        val disclaimerText = findViewById<TextView>(R.id.disclaimerText)
        val btnAgree = findViewById<Button>(R.id.btnAgree)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnMoreInfo = findViewById<TextView>(R.id.btnMoreInfo)

//        securityManager = AppSecurityManager(this);

//        if (!securityManager!!.isUserAuthenticated) {
//            securityManager!!.requestAuthentication(this);
//        }

        appIcon.setImageResource(R.drawable.ic_launcher_background)  // Replace with your app icon resource
        disclaimerText.text = getString(R.string.disclaimer_text)

        btnAgree.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnCancel.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Exit App")
            builder.setMessage("Are you sure you want to exit? Canceling will close the app.")

            builder.setPositiveButton("YES") { _, _ ->
                finishAffinity()
                exitProcess(0)
            }
            builder.setNeutralButton("NO") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog: AlertDialog = builder.create()
            dialog.setCancelable(false)
            dialog.show()
        }


        btnMoreInfo.setOnClickListener {
            // Open a web page or activity with more information about T&Cs and Privacy Policy
            val intent = Intent(this, MoreInfoActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
//        if (!securityManager!!.isUserAuthenticated) {
//            securityManager!!.requestAuthentication(this)
//        }
    }
    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.appcompat.app.AppCompatActivity"
        )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        securityManager!!.handleAuthenticationResult(requestCode, resultCode)
//        if (!securityManager!!.isUserAuthenticated) {
//            // Handle the case where authentication failed
//            finish() // or take other appropriate action
//        }
    }
}
