package co.za.clementine.mastersapp.profile.apps.install

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import co.za.clementine.mastersapp.DeviceOwnerReceiver
import java.io.File

class WorkProfileInstaller(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    private val componentName: ComponentName by lazy {
        ComponentName(context, DeviceOwnerReceiver::class.java)
    }

    fun installApkInWorkProfile(apkFile: File) {

        if (isWorkProfileEnabled()) {
            val apkUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = apkUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                showToast("Failed to install APK")
            }
        } else {
            showToast("Work profile is not enabled")
        }
    }

    private fun isWorkProfileEnabled(): Boolean {
        return devicePolicyManager.isProfileOwnerApp(context.packageName)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
