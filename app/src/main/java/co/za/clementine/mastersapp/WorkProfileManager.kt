package co.za.clementine.mastersapp

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.UserManager
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.za.clementine.mastersapp.exceptions.MyCustomException
import kotlinx.coroutines.delay

class WorkProfileManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val adminComponent: ComponentName
) {

    fun workProfileExist(): Boolean {
        try {
            if (!isMultipleUsersEnabled(context)) {
//                showEnableMultipleUsersDialog()
                throw MyCustomException(/*"security policy exception"*/)
            }
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                val existingProfiles = dpm.getSecondaryUsers(adminComponent)
                return (existingProfiles.size > 0)
            }
        } catch (e: SecurityException) {
//            makeToast("Security exception occurred")
            e.printStackTrace()
        } catch (e: Exception) {
//            makeToast("An error occurred")
            e.printStackTrace()
        }
        return false
    }

    fun createWorkProfile() {
        try {
            if (!isMultipleUsersEnabled(context)) {
                showEnableMultipleUsersDialog()
                throw MyCustomException(/*"security policy exception"*/)
            }
            // Check if the app is already a device owner
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                if (!workProfileExist()) {
//                    val flags = DevicePolicyManager.SKIP_SETUP_WIZARD or
//                                DevicePolicyManager.MAKE_USER_EPHEMERAL or
//                                DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
                    // Create a work profile
                    val workProfile = dpm.createAndManageUser(
                        adminComponent, "Work Profile", adminComponent,
                        null, 0 /*DevicePolicyManager.MAKE_USER_EPHEMERAL*/
                    )
//                        null, flags)

                    if (workProfile != null) {
                        makeToast("Work profile creation initiated")
                    } else {
                        makeToast("Failed to create work profile")
                    }
                }
            } else {
                makeToast("App is not a device owner")
            }
        } catch (e: SecurityException) {
//            makeToast("Security exception occurred")
            throw MyCustomException(/*"Security exception occurred"*/)
        } catch (e: Exception) {
//            makeToast("An error occurred")
            e.printStackTrace()
            throw MyCustomException(/*"An error occurred"*/)
        }
    }

    fun isMultipleUsersEnabled(context: Context): Boolean {
        return try {
            val pref = Settings.Global.getInt(context.contentResolver, "user_switcher_enabled", 0)
            pref == 1 && UserManager.supportsMultipleUsers()
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            throw MyCustomException(/*"security policy exception"*/)
        }
    }

    fun showEnableMultipleUsersDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enable Multiple Users")
        builder.setMessage(
            "To use this feature, you need to enable the 'Multiple users' setting on your device. " +
                    "Please follow these steps:\n\n1. Open Settings.\n2. Navigate to System settings.\n3. " +
                    "Access Additional settings.\n4. Enable Multiple users."
        )
        builder.setPositiveButton("Open Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_SETTINGS)
            context.startActivity(intent)
        }
        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
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
            makeToast("Settings not found")
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
