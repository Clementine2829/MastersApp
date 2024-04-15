package co.za.clementine.mastersapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import android.content.Intent

class WorkProfileManager(private val context: Context) {

    fun createWorkProfile() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)

        try {
            // Check if the app is already a device owner
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                // Get existing secondary users (profiles)
                val existingProfiles = dpm.getSecondaryUsers(adminComponent)
                if (existingProfiles.size > 0) {
                    // Another profile already exists, so we cannot create another one
                    Toast.makeText(context, "Only one additional profile is allowed", Toast.LENGTH_SHORT).show()
                } else {
                    // Create a work profile
                    val workProfile = dpm.createAndManageUser(adminComponent, "Work Profile", adminComponent,
                        null, DevicePolicyManager.MAKE_USER_EPHEMERAL)

                    if (workProfile != null) {
                        // Work profile created successfully, the setup wizard should be triggered automatically
                        Toast.makeText(context, "Work profile creation initiated", Toast.LENGTH_SHORT).show()
                    } else {
                        // Failed to create work profile
                        Toast.makeText(context, "Failed to create work profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Handle case where app is not a device owner
                Toast.makeText(context, "App is not a device owner", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            // Handle security exceptions
            Toast.makeText(context, "Security exception occurred", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle other exceptions
            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
        }
    }


    fun navigateToWorkProfileSettings() {
        val intent = Intent()

        // Intent action for opening settings
        intent.action = run {
            // For Android Nougat and above, open the work profile settings
            Intent.ACTION_APPLICATION_PREFERENCES
        }

        // Check if the intent can be resolved
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Handle case where the settings activity is not found
            Toast.makeText(context, "Settings not found", Toast.LENGTH_SHORT).show()
        }
    }
}
