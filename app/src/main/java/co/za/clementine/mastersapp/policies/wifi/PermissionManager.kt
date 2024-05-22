package co.za.clementine.mastersapp.policies.wifi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.system.exitProcess

class PermissionManager(private val activity: Activity) {

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1
        const val REQUEST_CODE_WRITE_SETTINGS = 2
    }

    fun requestNecessaryPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CHANGE_NETWORK_STATE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsToRequest.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        }

        if (!Settings.System.canWrite(activity)) {
//            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
//            intent.data = Uri.parse("package:${activity.packageName}")
//            activity.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
            showWriteSettingsDialog()
        }
    }
    private fun showWriteSettingsDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("SYSTEM SETTINGS permission is required to enforce secure Wi-Fi policies. Would you like to go to the settings to enable this permission?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                navigateToWriteSettings()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
//                activity.finishAffinity()
                exitProcess(0)
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToWriteSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:${activity.packageName}")
        activity.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
    }

    fun showPermissionPopupAndNavigateToSettings() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("To continue using this app, please grant the necessary permissions manually in the device settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                navigateToAppSettings()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray, onPermissionGranted: () -> Unit) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionGranted()
            } else {
                AlertDialog.Builder(activity)
                    .setTitle("Permission Required")
                    .setMessage("Necessary permissions are required to enforce secure Wi-Fi policies.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    fun handleWriteSettingsResult(requestCode: Int, onWriteSettingsGranted: () -> Unit) {
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Settings.System.canWrite(activity)) {
                onWriteSettingsGranted()
            } else {
                AlertDialog.Builder(activity)
                    .setTitle("Permission Required")
                    .setMessage("SYSTEM SETTINGS permission is required to enforce secure Wi-Fi policies.")
                    .setPositiveButton("Try Again") { dialog, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        intent.data = Uri.parse("package:${activity.packageName}")
                        activity.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        activity.finishAffinity()
                        exitProcess(0)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
}
