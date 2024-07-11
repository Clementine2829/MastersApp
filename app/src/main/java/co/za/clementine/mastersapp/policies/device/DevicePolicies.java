package co.za.clementine.mastersapp.policies.device;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class DevicePolicies extends PoliciesManager {
    private final int _passwordMinLength = 8;
    private final long _passwordExpirationTimeout = 2L * 60L * 1000L; // 5 minutes
    private final int _maxFailedPasswordsForWipe = 2;
    private final long timeoutMillis = 100000; // 100 seconds

    public DevicePolicies(Context context) {
        super(context);
        this.context = context;
    }

    public boolean isPasswordSet() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            int passwordQuality = devicePolicyManager.getPasswordQuality(componentName);
            return passwordQuality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        } else {
            return false;
        }
    }

    public boolean isDevicePasswordSetAccordingToPolicies() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            return devicePolicyManager.isActivePasswordSufficient();
        }
        return false;
    }

    public boolean areSecurityPoliciesEnforced() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            int passwordQuality = devicePolicyManager.getPasswordQuality(componentName);
            int passwordMinLength = devicePolicyManager.getPasswordMinimumLength(componentName);
            long passwordExpirationTimeout = devicePolicyManager.getPasswordExpirationTimeout(componentName);
            int maxFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(componentName);

            boolean isComplexPasswordRequired = passwordQuality == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX ||
                    passwordQuality == DevicePolicyManager.PASSWORD_QUALITY_COMPLEX;
            boolean isMinLengthEnforced = passwordMinLength >= _passwordMinLength;
            boolean isExpirationTimeoutSet = passwordExpirationTimeout == _passwordExpirationTimeout;
            boolean isMaxFailedPasswordsSet = maxFailedPasswordsForWipe == _maxFailedPasswordsForWipe;

            return isPasswordSet() && isComplexPasswordRequired && isMinLengthEnforced &&
                    isExpirationTimeoutSet && isMaxFailedPasswordsSet;
        } else {
            return false;
        }
    }


    public void setPasswordSecurityPolicies() {
        try {
            Thread.sleep(2000);
            if (devicePolicyManager.isAdminActive(componentName)) {
                try {
                    devicePolicyManager.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX);
                    devicePolicyManager.setPasswordQuality(componentName, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
                    devicePolicyManager.setPasswordMinimumLength(componentName, _passwordMinLength);
                    // Set password expiration timeout to 3 months (90 days)
                    //                devicePolicyManager.setPasswordExpirationTimeout(componentName, 90L * 24L * 60L * 60L * 1000L);  // 90 days
                    devicePolicyManager.setPasswordExpirationTimeout(componentName, _passwordExpirationTimeout);
                    devicePolicyManager.setMaximumFailedPasswordsForWipe(componentName, _maxFailedPasswordsForWipe);
                    showToast("High security policies set");
                } catch (SecurityException e) {
                    showToast("Failed to set high security policies: " + e.getMessage());
                }
            } else {
                showToast("Device Admin not active");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isStorageEncryptionEnforced() {
        int encryptionStatus = devicePolicyManager.getStorageEncryptionStatus();
        return encryptionStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER;
    }

    public void enforceStorageEncryption() {
        try {
            Thread.sleep(2000);
            if (devicePolicyManager.isAdminActive(componentName)) {
                if (devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
                    devicePolicyManager.setStorageEncryption(componentName, true);
                    showToast("Storage encryption enforced " + devicePolicyManager.getStorageEncryptionStatus());
                } else {
                    showToast("Storage encryption is not supported on this device");
                }
            } else {
                showToast("Device Admin not active");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isScreenTimeoutEnforced() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            long currentTimeoutMillis = devicePolicyManager.getMaximumTimeToLock(componentName);
            return currentTimeoutMillis >= timeoutMillis;
        } else {
            showToast("Device Admin not active");
            return false;
        }
    }

    public void setScreenTimeoutPolicy() {
        try {
            Thread.sleep(2000);
            if (devicePolicyManager.isAdminActive(componentName)) {
                devicePolicyManager.setMaximumTimeToLock(componentName, timeoutMillis);
                showToast("Screen timeout policy set to " + (timeoutMillis / 1000) + " seconds");
            } else {
                showToast("Device Admin not active");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


