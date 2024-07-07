package co.za.clementine.mastersapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.Utils.Companion.goToMainActivity
import co.za.clementine.mastersapp.Utils.Companion.goToMoreInfoActivity
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

        securityManager = AppSecurityManager(this);

        if (!securityManager!!.isUserAuthenticated) {
            securityManager!!.requestAuthentication(this);
        }

        appIcon.setImageResource(R.drawable.ic_launcher_background)  // Replace with your app icon resource
        disclaimerText.text = getString(R.string.disclaimer_text)

        btnAgree.setOnClickListener {
            goToMainActivity(this)
        }
        btnCancel.setOnClickListener {
            showExitConfirmationDialog("Are you sure you want to exit? Canceling will close the app.")
        }
        btnMoreInfo.setOnClickListener {
            goToMoreInfoActivity(this)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog("")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (!securityManager!!.isUserAuthenticated) {
            securityManager!!.requestAuthentication(this)
        }
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        securityManager!!.handleAuthenticationResult(requestCode, resultCode)
        if (!securityManager!!.isUserAuthenticated) {
            // Handle the case where authentication failed
            finish() // or take other appropriate action
        }
    }

    private fun showExitConfirmationDialog(msg: String) {
        val message =
            msg.ifEmpty { "Are you sure you want to exit? This action will close the app." }
        AlertDialog.Builder(this).apply {
            setTitle("Exit App")
            setMessage(message)
            setPositiveButton("Yes") { _, _ ->
                finishAffinity()
                exitProcess(0)
            }
            setNeutralButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            setCancelable(false)
            show()
        }
    }
}
