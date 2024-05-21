package co.za.clementine.mastersapp.permissions

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class StorageAccessPermission(private val activity: AppCompatActivity) {

    @RequiresApi(Build.VERSION_CODES.R)
    private val manageStoragePermissionLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!isExternalStorageManager()) {
//                Toast.makeText(activity, "Permission granted", Toast.LENGTH_SHORT).show()
//            } else {
                showPermissionDeniedDialog()
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    fun checkAndRequestPermission() {
        if (!isExternalStorageManager()) {
            showPermissionRequestDialog()
//        } else {
//            // Permission already granted, you can access all files
//            Toast.makeText(activity, "All files access already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isExternalStorageManager(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // For devices running below Android 11
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showPermissionRequestDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Storage Access Required")
            .setMessage("This app needs access to all files to function properly. Please grant the permission in the next screen.")
            .setPositiveButton("Allow") { _, _ ->
                requestManageExternalStoragePermission()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                showPermissionDeniedDialog()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permission Denied")
            .setMessage("Without the required permission, the app cannot function properly. Please grant the permission to continue using the app.")
            .setPositiveButton("Try Again") { _, _ ->
                showPermissionRequestDialog()
            }
            .setNegativeButton("Close App") { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageExternalStoragePermission() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${activity.packageName}")
        manageStoragePermissionLauncher.launch(intent)
    }
}
