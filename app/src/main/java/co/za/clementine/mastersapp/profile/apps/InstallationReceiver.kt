package co.za.clementine.mastersapp.profile.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import android.widget.Toast

class InstallationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val packageName = intent.getStringExtra("packageName")
        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Toast.makeText(context, "Installation successful for $packageName", Toast.LENGTH_SHORT).show()
                Log.d("InstallationReceiver", "Installation successful for $packageName")
            }
            PackageInstaller.STATUS_FAILURE -> {
                Toast.makeText(context, "Installation failed for $packageName", Toast.LENGTH_SHORT).show()
                Log.e("InstallationReceiver", "Installation failed for $packageName")
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Toast.makeText(context, "Installation failed for $packageName: $message", Toast.LENGTH_SHORT).show()
                Log.e("InstallationReceiver", "Installation failed for $packageName: $message")
            }
        }
    }
}
