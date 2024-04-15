package co.za.clementine.mastersapp

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object UserManagerCompat {
    fun getUserId(userHandle: UserHandle): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            userHandle.hashCode()
        } else {
            // If userHandle.hashCode() is not available, return a default value (e.g., 0)
            0
        }
    }
}

class ProfileSelectionDialog(private val context: Context) {
    private val REQUEST_CODE_MANAGE_USERS = 100  // You can choose any unique integer value

    fun show() {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        val profiles = userManager.userProfiles
        val profileNames = mutableListOf<String>()

        profiles.forEach { profile ->
            if (userManager.isUserUnlocked(profile)) {
                profileNames.add("User ${profile.hashCode()}")
            }
        }

        val arrayAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, profileNames)
        val listView = ListView(context)
        listView.adapter = arrayAdapter

        val alertDialog = android.app.AlertDialog.Builder(context)
            .setTitle("Select Profile")
            .setView(listView)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Open user switcher screen when an item in the list is clicked
        listView.setOnItemClickListener { _, _, position, _ ->
            val userHandle = profiles[position]

            // Call the switchToProfile method to switch to the new profile
            switchToProfile(userHandle)

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Separate method for switching to the new profile

//    private fun switchToProfile(userHandle: UserHandle) {
//        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
//
//        // Ensure ActivityManager is not null and the switchUser method is available
//        if (am != null) {
//            val userId = UserManagerCompat.getUserId(userHandle)
//
//            am.switchUser(userId)
//            Toast.makeText(context, "Switching to the new profile...", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "Cannot switch profile", Toast.LENGTH_SHORT).show()
//        }
//    }

    // Inside your switchToProfile method
    private fun switchToProfile(userHandle: UserHandle) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponentName = ComponentName(context, DeviceOwnerReceiver::class.java)

        if (dpm.isDeviceOwnerApp(context.packageName)) {
            val userHandles = dpm.getSecondaryUsers(adminComponentName)
            if (userHandles.isNotEmpty()) {
                val targetUserHandle = userHandles[0]
                dpm.switchUser(adminComponentName, targetUserHandle)
            } else{
                Toast.makeText(context, "User empty", Toast.LENGTH_SHORT).show()
            }
        } else{
            System.out.println("App is not device manager")
        }



//        // Check if app has MANAGE_USERS permission
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.MANAGE_USERS)
//            != PackageManager.PERMISSION_GRANTED) {
//            // Request permission if not granted
//            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.MANAGE_USERS), REQUEST_CODE_MANAGE_USERS)
//            return
//        }
//
//        // Try the USER_SWITCH intent first (may not be available on all devices)
//        val switchUserIntent = Intent("android.intent.action.USER_SWITCH")
//        switchUserIntent.putExtra("android.intent.extra.user_handle", userHandle)
//        context.startActivity(switchUserIntent)
//
//        if (isIntentAvailable(context, switchUserIntent)) {
//            // Success, user switching might happen
//            Toast.makeText(context, "Switching to the new profile...", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Fallback: Open user settings if USER_SWITCH fails
//        val settingsIntent = Intent("android.settings.USER_SETTINGS")
//        context.startActivity(settingsIntent)
//        Toast.makeText(context, "Unable to switch directly, opening user settings...", Toast.LENGTH_SHORT).show()
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }



}
