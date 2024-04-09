package co.za.clementine.mastersapp

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    companion object {
        const val PROVISIONING_REQUEST_CODE = 123 // Arbitrary request code
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun setupManagedProfile(activity: Activity) {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val userManager = activity.getSystemService(Context.USER_SERVICE) as UserManager
        val adminComponent = ComponentName(activity, MyDeviceAdminReceiver::class.java)

        if (!dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            // Provisioning is not allowed by device policy or user restrictions
            // Handle this according to your app's requirements
            return
        }

        Toast.makeText(activity, "Call em ", Toast.LENGTH_SHORT).show()

        if (userManager.isManagedProfile) {
            // Managed profile already exists, no need to provision again
            // You can handle this scenario based on your app's logic
            Toast.makeText(activity, "Profile already exist", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_USER_CONSENT, true)
        }

        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED, true)

        // Start the provisioning process from the activity
        activity.startActivityForResult(intent, PROVISIONING_REQUEST_CODE)
    }
}
