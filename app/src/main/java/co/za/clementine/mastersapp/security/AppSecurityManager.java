package co.za.clementine.mastersapp.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class AppSecurityManager {
    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL = 1;
    private KeyguardManager keyguardManager;
    private boolean isAuthenticated;

    public AppSecurityManager(Context context) {
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    public void requestAuthentication(AppCompatActivity activity) {
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm your screen lock");
        if (intent != null) {
            activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL);
        }
    }

    public void handleAuthenticationResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL) {
            isAuthenticated = (resultCode == AppCompatActivity.RESULT_OK);
        }
    }

    public boolean isUserAuthenticated() {
        return isAuthenticated;
    }
}
