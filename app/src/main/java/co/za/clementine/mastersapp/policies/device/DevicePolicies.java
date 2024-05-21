package co.za.clementine.mastersapp.policies.device;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import android.widget.Toast;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class DevicePolicies extends PoliciesManager {

    public DevicePolicies(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(context, DeviceOwnerReceiver.class);
        this.context = context;

        setPasswordSecurityPolicies();
        enforceStorageEncryption();
        verifyPolicies();
        setScreenTimeoutPolicy(10000); // 10 seconds
//        setScreenTimeoutPolicy(3000); // 3 seconds
    }


    public void setPasswordSecurityPolicies() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            try {
                // Only allow PIN and password
                devicePolicyManager.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX);
                devicePolicyManager.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);

                // Set minimum PIN length to 8 digits
                devicePolicyManager.setPasswordMinimumLength(componentName, 8);

                // Set password expiration timeout to 3 months (90 days)
//                devicePolicyManager.setPasswordExpirationTimeout(componentName, 90L * 24L * 60L * 60L * 1000L);  // 90 days

                // Example: Set password expiration timeout to 5 minutes for testing
                devicePolicyManager.setPasswordExpirationTimeout(componentName, 2L * 60L * 1000L);  // 5 minutes

                // Set maximum failed passwords for wipe
                devicePolicyManager.setMaximumFailedPasswordsForWipe(componentName, 10);

                showToast("High security policies set");
            } catch (SecurityException e) {
                showToast("Failed to set high security policies: " + e.getMessage());
            }
        } else {
            showToast("Device Admin not active");
        }
    }
    private void enforceStorageEncryption() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
                devicePolicyManager.setStorageEncryption(componentName, true);
                showToast("Storage encryption enforced");
            } else {
                showToast("Storage encryption is not supported on this device");
            }
        } else {
            showToast("Device Admin not active");
        }
    }

    public void setScreenTimeoutPolicy(long timeoutMillis) {
        if (devicePolicyManager.isAdminActive(componentName)) {
            devicePolicyManager.setMaximumTimeToLock(componentName, timeoutMillis);
            showToast("Screen timeout policy set to " + (timeoutMillis / 1000) + " seconds");
        } else {
            showToast("Device Admin not active");
        }
    }






}


