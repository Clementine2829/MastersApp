package co.za.clementine.mastersapp.policies.device;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public abstract class PoliciesManager {

    protected  Context context;

    public enum PasswordQuality {
        UNSPECIFIED("UNSPECIFIED"),
        BIOMETRIC_WEAK("BIOMETRIC WEAK"),
        SOMETHING("SOMETHING"),
        NUMERIC("NUMERIC"),
        COMPLEX("COMPLEX"),
        ALPHANUMERIC("ALPHANUMERIC"),
        UNKNOWN("UNKNOWN");

        private final String value;

        PasswordQuality(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public void verifyPasswordPolicies(DevicePolicyManager devicePolicyManager, ComponentName componentName) {
        int passwordQuality = devicePolicyManager.getPasswordQuality(componentName);
        int maxFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(componentName);

        PasswordQuality passwordQualityEnum;
        switch (passwordQuality) {
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                passwordQualityEnum = PasswordQuality.UNSPECIFIED;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
                passwordQualityEnum = PasswordQuality.BIOMETRIC_WEAK;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                passwordQualityEnum = PasswordQuality.SOMETHING;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                passwordQualityEnum = PasswordQuality.NUMERIC;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                passwordQualityEnum = PasswordQuality.COMPLEX;
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                passwordQualityEnum = PasswordQuality.ALPHANUMERIC;
                break;
            default:
                passwordQualityEnum = PasswordQuality.UNKNOWN;
                break;
        }

        showToast("Password Quality: " + passwordQualityEnum);
        showToast("Max Failed Passwords for Wipe: " + maxFailedPasswordsForWipe);
    }
    public static void removeDeviceAdmin(DevicePolicyManager devicePolicyManager, ComponentName componentName, Context context) {
//        if (devicePolicyManager.isAdminActive(componentName)) {
//            devicePolicyManager.removeActiveAdmin(componentName);
//            showToast(context, "Device admin removed");
//        } else {
//            showToast(context, "Device admin not active");
//        }
        if (devicePolicyManager.isAdminActive(componentName)) {
            // Reset password policies
            devicePolicyManager.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
//            devicePolicyManager.setPasswordMinimumLength(componentName, 0);
            devicePolicyManager.setPasswordExpirationTimeout(componentName, 0);
            devicePolicyManager.setMaximumFailedPasswordsForWipe(componentName, 0);

            // Reset encryption policy
            devicePolicyManager.setStorageEncryption(componentName, false);

            // Reset other policies
            devicePolicyManager.setMaximumTimeToLock(componentName, Long.MAX_VALUE);
            devicePolicyManager.setCameraDisabled(componentName, false);

        }
    }

    public static void showPolicyDialog(Context context) {
        String message = "The following policies must be set:\n\n" +
                "1. Set complex password on device.\n" +
                "   -> Go to Settings > Security > Screen Lock and set a strong password.\n\n" +
                "2. Device storage must be encrypted.\n" +
                "   -> Go to Settings > Security > Encryption & credentials and encrypt your device if it is not already encrypted.\n";

        new AlertDialog.Builder(context)
                .setTitle("Required Policies")
                .setMessage(message)
                .setPositiveButton("OPEN SETTINGS", (dialog, which) -> openDeviceSettings(context))
                .setNeutralButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .create()
                .show();
    }
    private static void openDeviceSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
