package co.za.clementine.mastersapp.network

import android.app.KeyguardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

class AppSecurityManager(private val context: Context) {
    companion object {
        const val REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL = 1
    }

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private var isAuthenticated = false

    fun requestAuthentication(activity: AppCompatActivity) {
        val intent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm your screen lock")
        if (intent != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL)
        }
    }

    fun handleAuthenticationResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL) {
            isAuthenticated = (resultCode == AppCompatActivity.RESULT_OK)
        }
    }

    fun isUserAuthenticated(): Boolean {
        return isAuthenticated
    }
}
