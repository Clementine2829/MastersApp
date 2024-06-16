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
import kotlinx.coroutines.delay

class WorkProfileManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val adminComponent: ComponentName) {

    fun workProfileExist(): Boolean {
        try {
            if (!isMultipleUsersEnabled(context)) {
                showEnableMultipleUsersDialog()
                return false
            }
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                val existingProfiles = dpm.getSecondaryUsers(adminComponent)
                return (existingProfiles.size > 0)
            }
        } catch (e: SecurityException) {
                makeToast("Security exception occurred")
        } catch (e: Exception) {
            makeToast("An error occurred")
        }
        return false
    }

    suspend fun createWorkProfile() {
        delay(2000)
        try {
            if(!isMultipleUsersEnabled(context)){
                showEnableMultipleUsersDialog()
                return
            }
            // Check if the app is already a device owner
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                if (!workProfileExist()) {
//                    val flags = DevicePolicyManager.SKIP_SETUP_WIZARD or
//                                DevicePolicyManager.MAKE_USER_EPHEMERAL or
//                                DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED
                    // Create a work profile
                    val workProfile = dpm.createAndManageUser(adminComponent, "Work Profile", adminComponent,
                        null,0 /*DevicePolicyManager.MAKE_USER_EPHEMERAL*/)
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
            makeToast("Security exception occurred")
        } catch (e: Exception) {
            makeToast("An error occurred")
        }
    }


    private fun isMultipleUsersEnabled(context: Context): Boolean {
        return try {
            val pref = Settings.Global.getInt(context.contentResolver, "user_switcher_enabled")
            pref == 1 && UserManager.supportsMultipleUsers()
        } catch (e: Settings.SettingNotFoundException) {
            println("user_switcher_enabled setting not found: ${e.message}")
            false
        }
    }

    private fun showEnableMultipleUsersDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enable Multiple Users")
        builder.setMessage("To use this feature, you need to enable the 'Multiple users' setting on your device. " +
                "Please follow these steps:\n\n1. Open Settings.\n2. Navigate to System settings.\n3. " +
                "Access Additional settings.\n4. Enable Multiple users.")
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

    private fun makeToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    public fun lockProfile() {
//        if (dpm.isProfileOwnerApp("co.za.clementine.mastersapp")) {
//            dpm.lockNow()
//        }
//        if (dpm.isProfileOwnerApp("co.za.clementine.mastersapp")) {
//            dpm.setPasswordQuality(adminComponent, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX)
//            dpm.setPasswordMinimumLength(adminComponent, 8)
//            showToast(context, "Locked with password")
//        }



        // Create or retrieve the MasterKey
        val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                    .build()
            )
            .build()

// Create an EncryptedSharedPreferences instance
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "my_secret_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

// Save a password
        sharedPreferences.edit()
            .putString("password", "my_password")
            .apply()

// Retrieve the password
        val password = sharedPreferences.getString("password", "")



    }


}
