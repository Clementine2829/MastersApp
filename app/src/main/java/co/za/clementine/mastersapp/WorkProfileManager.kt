package co.za.clementine.mastersapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

class WorkProfileManager(private val context: Context) {

    fun createWorkProfile() {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)

        // Check if the app is already a device owner
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            // Check if the work profile is already created
            if (!dpm.isProfileOwnerApp(context.packageName)) {
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
            } else {
                // Work profile already exists
                Toast.makeText(context, "Work profile already exists", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle case where app is not a device owner
            Toast.makeText(context, "App is not a device owner", Toast.LENGTH_SHORT).show()
        }
    }
}


//import android.app.admin.DevicePolicyManager
//import android.content.ComponentName
//import android.content.Context
//import android.os.UserHandle
//import android.widget.Toast
//
//class WorkProfileManager(private val context: Context) {
//
//    fun createWorkProfile() {
//        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
//        val adminComponent = ComponentName(context, DeviceOwnerReceiver::class.java)
//
//        // Check if the app is already a device owner
//        if (dpm.isDeviceOwnerApp(context.packageName)) {
//            // Create a work profile
//            val workProfile = dpm.createAndManageUser(adminComponent, "Work Profile", adminComponent,
//                null, DevicePolicyManager.SKIP_SETUP_WIZARD)
//
//            if (workProfile != null) {
//                // Apply policies to the work profile
//                // For example, set restrictions or configure security policies
//                // dpm.setCameraDisabled(adminComponent, workProfile, true)
//                // dpm.setKeyguardDisabled(adminComponent, workProfile, true)
//
//                // Handle other tasks such as managing apps and data within the work profile
//                Toast.makeText(context, "Work profile created successfully", Toast.LENGTH_SHORT).show()
//            } else {
//                // Handle failure to create work profile
//                Toast.makeText(context, "Failed to create work profile", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            // Handle case where app is not a device owner
//            Toast.makeText(context, "App is not a device owner", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
