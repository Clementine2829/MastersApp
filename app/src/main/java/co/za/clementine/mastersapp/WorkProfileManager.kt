package co.za.clementine.mastersapp

import android.app.admin.DeviceAdminInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import android.content.Intent

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class WorkProfileManager(private val context: Context) {

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)

    fun createWorkProfile() {

        try {
            // Check if the app is already a device owner
            if (dpm.isDeviceOwnerApp(context.packageName)) {
                // Get existing secondary users (profiles)
                val existingProfiles = dpm.getSecondaryUsers(adminComponent)
                if (existingProfiles.size > 0) {
                    // Another profile already exists, so we cannot create another one
                    Toast.makeText(context, "Only one additional profile is allowed", Toast.LENGTH_SHORT).show()
                } else {
                    val flags = DevicePolicyManager.SKIP_SETUP_WIZARD or
                                DevicePolicyManager.MAKE_USER_EPHEMERAL
                    // Create a work profile
                    val workProfile = dpm.createAndManageUser(adminComponent, "Work Profile", adminComponent,
                        null,0 /*DevicePolicyManager.MAKE_USER_EPHEMERAL*/)
//                        null, flags)

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
