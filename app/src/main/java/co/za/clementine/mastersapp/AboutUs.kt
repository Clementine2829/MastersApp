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
import co.za.clementine.mastersapp.Utils.Companion.goToMainActivity

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        supportActionBar?.title = "About Us"

        val tvAboutUs = findViewById<TextView>(R.id.tvAboutUs)
        val aboutUsContent = getString(R.string.about_us_content)

        // Display about us content as HTML
        tvAboutUs.text = Html.fromHtml(aboutUsContent, Html.FROM_HTML_MODE_COMPACT)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToMainActivity(this@AboutUs)
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_icon, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_button -> {
                goToMainActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
