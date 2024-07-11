package co.za.clementine.mastersapp

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast

class DeviceOwnerReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName =
            ComponentName(context.applicationContext, DeviceOwnerReceiver::class.java)

        manager.setProfileName(componentName, context.getString(R.string.profile_name_clementine))
        manager.setProfileEnabled(componentName)

        val userHandle: UserHandle? = intent.getParcelableExtra(Intent.EXTRA_USER)
        if (userHandle != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (launchIntent != null) {
                startActivityAsUser(context, launchIntent, userHandle)
            }
        }
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Admin disable requested..."
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        showToast(context, "Device administrator enabled...")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        showToast(context, "Device administrator disabled...")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        showToast(context, "Lock task mode entering now...")
        super.onEnabled(context, intent)
        applyDevicePolicies(context)
    }

    private fun applyDevicePolicies(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)

        // Display a custom lock screen message
        dpm.setOrganizationName(adminComponent, "ShildDroid Enterprise")
        dpm.setDeviceOwnerLockScreenInfo(
            adminComponent,
            "This device is managed by Masters App. Contact IT for assistance."
        )

        disableCamera(dpm, adminComponent)

        showToast(context, "Device policies applied")
    }

    private fun disableCamera(dpm: DevicePolicyManager, adminComponent: ComponentName) {
        dpm.setCameraDisabled(adminComponent, true)
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        showToast(context, "Lock task mode exiting")
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun startActivityAsUser(context: Context, intent: Intent, userHandle: UserHandle) {
        try {
            val method = Context::class.java.getMethod(
                "startActivityAsUser",
                Intent::class.java,
                UserHandle::class.java
            )
            method.invoke(context, intent, userHandle)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Failed to start activity in work profile")
        }
    }
}
