package co.za.clementine.mastersapp.policies.device;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public abstract class PoliciesManager {
    protected static DevicePolicyManager devicePolicyManager;
    protected static ComponentName componentName;
    protected  Context context;

    public void verifyPolicies() {
        int passwordQuality = devicePolicyManager.getPasswordQuality(componentName);
        int maxFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(componentName);

        String passwordQualityStr;
        switch (passwordQuality) {
            case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
                passwordQualityStr = "UNSPECIFIED";
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
                passwordQualityStr = "BIOMETRIC WEAK";
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                passwordQualityStr = "SOMETHING";
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                passwordQualityStr = "NUMERIC";
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
                passwordQualityStr = "COMPLEX";
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                passwordQualityStr = "ALPHANUMERIC";
                break;
            default:
                passwordQualityStr = "UNKNOWN";
                break;
        }

        showToast("Password Quality: " + passwordQualityStr);
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

    public static boolean arePoliciesApplied(DevicePolicyManager devicePolicyManager, ComponentName componentName) {
        // Check if the necessary policies are set
        int passwordQuality = devicePolicyManager.getPasswordQuality(componentName);
        int minPasswordLength = devicePolicyManager.getPasswordMinimumLength(componentName);
        int encryptionStatus = devicePolicyManager.getStorageEncryptionStatus();

        boolean isPasswordQualitySufficient = passwordQuality >= DevicePolicyManager.PASSWORD_QUALITY_COMPLEX;
        boolean isPasswordLengthSufficient = minPasswordLength >= 8;
        boolean isStorageEncrypted = encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE;

        return isPasswordQualitySufficient && isPasswordLengthSufficient && isStorageEncrypted;
    }

    public static void showPolicyDialog(Context context) {
        String message = "The following policies must be set:\n\n" +
                "1. Password Quality: Must be complex.\n" +
                "   - Go to Settings > Security > Screen Lock and set a strong password.\n\n" +
                "2. Minimum Password Length: Must be at least 8 characters.\n" +
                "   - Ensure your password is at least 8 characters long.\n\n" +
                "3. Storage Encryption: Device storage must be encrypted.\n" +
                "   - Go to Settings > Security > Encryption & credentials and encrypt your device if it is not already encrypted.\n";

        new AlertDialog.Builder(context)
                .setTitle("Required Policies")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("SETTINGS", (dialog, which) -> openDeviceSettings(context))
                .setCancelable(false)
                .create()
                .show();
    }
    private static void openDeviceSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
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
