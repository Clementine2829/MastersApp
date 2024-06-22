package co.za.clementine.mastersapp

import android.os.Bundle
import android.text.Html
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        supportActionBar?.hide()

        val tvAboutUs = findViewById<TextView>(R.id.tvAboutUs)
        val aboutUsContent = getString(R.string.about_us_content)

        // Display about us content as HTML
        tvAboutUs.text = Html.fromHtml(aboutUsContent, Html.FROM_HTML_MODE_COMPACT)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
            finish()
        }

    }
}
