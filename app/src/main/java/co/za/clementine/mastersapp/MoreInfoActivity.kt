package co.za.clementine.mastersapp

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MoreInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)

        val tvMoreInfo = findViewById<TextView>(R.id.tvMoreInfo)
        val moreInfoContent = getString(R.string.more_info_content)

        // Display T&Cs and Privacy Policy content as HTML
        tvMoreInfo.text = Html.fromHtml(moreInfoContent, Html.FROM_HTML_MODE_COMPACT)
    }
}
