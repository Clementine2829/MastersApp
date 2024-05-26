package co.za.clementine.mastersapp.profile.apps.install

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import co.za.clementine.mastersapp.DeviceOwnerReceiver

class WorkProfileAppInstaller1(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private val componentName: ComponentName by lazy {
        ComponentName(context, DeviceOwnerReceiver::class.java)
    }

    fun installAppInWorkProfile(packageName: String) {
        if (isWorkProfileEnabled()) {
            try {
                devicePolicyManager.installExistingPackage(componentName, packageName)
                showToast("Requested app installation in work profile")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to install app in work profile")
            }
        } else {
            showToast("Work profile is not enabled or your app is not the profile owner")
        }
    }

    private fun isWorkProfileEnabled(): Boolean {
        return devicePolicyManager.isProfileOwnerApp(context.packageName)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
