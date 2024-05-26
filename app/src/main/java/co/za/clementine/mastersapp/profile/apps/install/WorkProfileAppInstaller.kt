package co.za.clementine.mastersapp.profile.apps.install

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import co.za.clementine.mastersapp.DeviceOwnerReceiver

class WorkProfileAppInstaller(private val context: Context) {

    private val dpm: DevicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent: ComponentName = ComponentName(context, DeviceOwnerReceiver::class.java)

    fun installAppInWorkProfile(packageName: String) {
        try {
            // Check if the app is the profile owner in the work profile
            if (dpm.isProfileOwnerApp(context.packageName)) {
                // Install the app in the work profile
                dpm.installExistingPackage(adminComponent, packageName)
                showToast("App installed in work profile")
            } else {
                showToast("App is not profile owner in work profile")
            }
        } catch (e: Exception) {
            showToast("Failed to install app: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
