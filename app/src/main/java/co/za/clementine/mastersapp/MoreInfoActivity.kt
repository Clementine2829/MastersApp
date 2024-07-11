package co.za.clementine.mastersapp

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.Utils.Companion.goToDisclaimer
import co.za.clementine.mastersapp.Utils.Companion.goToMainActivity
import kotlin.system.exitProcess

class MoreInfoActivity : AppCompatActivity() {

    private var goToDisclamerScreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)

        supportActionBar?.title = "T&Cs and Privacy Policy"

        val tvMoreInfo = findViewById<TextView>(R.id.tvMoreInfo)
        val moreInfoContent = getString(R.string.more_info_content)

        tvMoreInfo.text = Html.fromHtml(moreInfoContent, Html.FROM_HTML_MODE_COMPACT)
        val activeScreen = intent.getStringExtra("activeScreen")
        if (activeScreen.equals("disclaimer")) goToDisclamerScreen = true

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_icon, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home,
            R.id.home_button -> {
                if (goToDisclamerScreen) {
                    goToDisclaimer(this)
                } else {
                    goToMainActivity(this)
                }
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
