package co.za.clementine.mastersapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

class ProfileSelectionDialog(private val context: Context) {
    private val REQUEST_CODE_MANAGE_USERS = 100  // You can choose any unique integer value

    fun show() {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
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

    // Inside your switchToProfile method
    private fun switchToProfile() {
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
    }


}
