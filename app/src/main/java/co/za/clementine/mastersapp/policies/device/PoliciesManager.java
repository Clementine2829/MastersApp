package co.za.clementine.mastersapp.policies.device;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
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
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.removeActiveAdmin(componentName);
            showToast(context, "Device admin removed");
        } else {
            showToast(context, "Device admin not active");
        }
    }

    public static boolean arePoliciesApplied() {
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
                .setCancelable(false)
                .create()
                .show();
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
