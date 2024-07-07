package co.za.clementine.mastersapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.Utils.Companion.goToDisclaimer
import kotlin.system.exitProcess

class MoreInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)
        supportActionBar?.hide()

        val tvMoreInfo = findViewById<TextView>(R.id.tvMoreInfo)
        val moreInfoContent = getString(R.string.more_info_content)

        // Display T&Cs and Privacy Policy content as HTML
        tvMoreInfo.text = Html.fromHtml(moreInfoContent, Html.FROM_HTML_MODE_COMPACT)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            goToDisclaimer(this)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToDisclaimer(this@MoreInfoActivity)
            }
        })
    }
}
