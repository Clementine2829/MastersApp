package co.za.clementine.mastersapp.profiles.switch_between

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserHandle
import android.os.UserManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import co.za.clementine.mastersapp.DeviceOwnerReceiver


class ProfileSelectionDialog(private val context: Context) {

    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponentName = ComponentName(context, DeviceOwnerReceiver::class.java)

    fun showAndSwitchToWorkProfile() {
        val profiles = userManager.userProfiles
        val profileNames = mutableListOf<String>()

        profiles.forEach { profile ->
            if (userManager.isUserUnlocked(profile)) {
                profileNames.add("Work profile ${profile.hashCode()}")
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
            switchToProfile()

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun switchToProfile() {
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            val userHandles = dpm.getSecondaryUsers(adminComponentName)
            if (userHandles.isNotEmpty()) {
                val targetUserHandle = userHandles[0]
                dpm.switchUser(adminComponentName, targetUserHandle)
            } else{
                Toast.makeText(context, "User empty", Toast.LENGTH_SHORT).show()
            }
        } else{
            println("App is not device manager")
        }
    }
//    fun switchToAdminProfile() {
//
//        println("Hi clementine 2, clicked... ")
//
//        if (dpm.isProfileOwnerApp(context.packageName)) {
//            val userHandles = userManager.userProfiles
//            if (userHandles.isNotEmpty()) {
//                val targetUserHandle = userHandles.find { userManager.isSystemUser(it) }
//                if (targetUserHandle != null) {
//                    // Note: switchUser is a hidden method and may not be accessible
//                    try {
//                        val switchUserMethod = dpm.javaClass.getMethod("switchUser", ComponentName::class.java, UserHandle::class.java)
//                        switchUserMethod.invoke(dpm, adminComponentName, targetUserHandle)
//                        Toast.makeText(context, "Switched to admin user", Toast.LENGTH_SHORT).show()
//                    } catch (e: Exception) {
//                        Toast.makeText(context, "Failed to switch user", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(context, "Admin user not found", Toast.LENGTH_SHORT).show()
//                }
//            } else{
//                Toast.makeText(context, "User empty", Toast.LENGTH_SHORT).show()
//            }
//        } else{
//            System.out.println("App is not device manager")
//        }
//    }

}
