package co.za.clementine.mastersapp

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.provider.Settings
import androidx.activity.ComponentActivity
import android.os.Bundle
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi

class DeviceOwnerReceiver : DeviceAdminReceiver() {

    companion object {
        const val PROVISIONING_REQUEST_CODE = 123 // Arbitrary request code
    }

    @Override
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val manager = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context.applicationContext, DeviceOwnerReceiver::class.java)

//        manager.setProfileName(componentName, context.getString(R.string.profile_name))
        manager.setProfileName(componentName, context.getString(R.string.profile_name))

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // Function to initiate the provisioning process for setting up a managed profile
    @RequiresApi(Build.VERSION_CODES.R)
    fun setupManagedProfile(activity: Activity) {
        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val userManager = activity.getSystemService(Context.USER_SERVICE) as UserManager
        val adminComponent = ComponentName(activity, DeviceOwnerReceiver::class.java)

        if (!dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
            // Provisioning is not allowed by device policy or user restrictions
            // Handle this according to your app's requirements
            return
        }

        if (userManager.isManagedProfile) {
            // Managed profile already exists, no need to provision again
            // You can handle this scenario based on your app's logic
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


    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        showToast(context, "Device administrator enabled")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        showToast(context, "Device administrator disabled")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        showToast(context, "Lock task mode entering")
        super.onEnabled(context, intent);
        applyDevicePolicies(context)
    }
    private fun applyDevicePolicies(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)

//        // Set password constraints
//        val passwordConstraints = Bundle().apply {
//            putInt(DevicePolicyManager.KEYguard_password_quality, DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC)
//            putInt(DevicePolicyManager.KEYguard_password_minimum_length, 8)
//            putInt(DevicePolicyManager.KEYguard_password_minimum_letters, 1)
//            putInt(DevicePolicyManager.KEYguard_password_minimum_non_letter, 1)
//            putInt(DevicePolicyManager.KEYguard_password_minimum_numeric, 1)
//        }
//
//        // Set password constraints
//        dpm.setPasswordMinimumRequirements(adminComponent, passwordConstraints)
//
//        // Restrict app installation to only allow installation from the Google Play Store
//        dpm.setSecureSetting(adminComponent, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")
//
//        // Set custom lock screen message
        dpm.setOrganizationName(adminComponent, "Masters App ")
////        dpm.setOrganizationOwnedSmallBusiness(true)
        dpm.setDeviceOwnerLockScreenInfo(adminComponent, "This device is managed by MyCompany. Contact IT support for assistance.")
//
        Toast.makeText(context, "Device policies applied", Toast.LENGTH_SHORT).show()
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        showToast(context, "Lock task mode exiting")
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

