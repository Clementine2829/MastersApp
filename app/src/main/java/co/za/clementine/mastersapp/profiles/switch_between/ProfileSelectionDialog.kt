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
import kotlinx.coroutines.delay
import kotlin.system.exitProcess


class ProfileSelectionDialog(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val adminComponentName: ComponentName) {
    fun switchToProfile() {
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            val userHandles = dpm.getSecondaryUsers(adminComponentName)
            if (userHandles.isNotEmpty()) {
                val targetUserHandle = userHandles[0]
                dpm.switchUser(adminComponentName, targetUserHandle)
                quitApp()
            } else{
                Toast.makeText(context, "User empty", Toast.LENGTH_SHORT).show()
            }
        } else{
            println("App is not device manager")
        }
    }

    private fun  quitApp(){
        val a:Activity = context as Activity
        a.finishAffinity()
        exitProcess(0)
    }
}
