package co.za.clementine.mastersapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent

class Utils {
    companion object{
        fun goToDisclaimer(context: Activity) {
            context.startActivity(Intent(context, DisclaimerActivity::class.java))
            context.finishAffinity()
        }
        fun goToMainActivity(context: Activity) {
            context.startActivity(Intent(context, MainActivity::class.java))
            context.finishAffinity()
        }
        fun goToMoreInfoActivity(context: Activity) {
            context.startActivity(Intent(context, MoreInfoActivity::class.java))
            context.finishAffinity()
        }
        fun goToAboutUsActivity(context: Activity) {
            context.startActivity(Intent(context, AboutUs::class.java))
            context.finishAffinity()
        }

        fun confirmPopUpAction(
            context: Context,
            title: String,
            message: String,
            foo1: () -> Unit,
            foo2: () -> Unit
        ) {
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("YES") { _, _ ->
                    foo1()
                }
                .setNeutralButton("NO") { dialog, _ ->
                    dialog.dismiss()
                    foo2()
                }
                .setCancelable(false)
                .create()
                .show()
        }

    }
}