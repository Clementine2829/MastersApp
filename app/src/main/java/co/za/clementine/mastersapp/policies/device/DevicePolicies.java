package co.za.clementine.mastersapp.policies.device;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import android.widget.Toast;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class DevicePolicies  {

    private final DevicePolicyManager devicePolicyManager;
    private final ComponentName componentName;
    private final Context context;
    public DevicePolicies(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(context, DeviceOwnerReceiver.class);
        this.context = context;
    }


    public void setHighSecurityPolicies() {
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
                e.printStackTrace();
            }
        } else {
            showToast("Device Admin not active");
        }
    }

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


    public void setWorkProfileRestrictions() {

        if (devicePolicyManager.isProfileOwnerApp(context.getPackageName())) {
//            // Set some restrictions
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_SHARE_LOCATION);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_UNINSTALL_APPS);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_ADD_USER);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_CELL_BROADCASTS);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_CONFIG_BLUETOOTH);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_CONFIG_WIFI);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CREATE_WINDOWS);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_FACTORY_RESET);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_INSTALL_APPS);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
            devicePolicyManager.addUserRestriction(componentName, UserManager.DISALLOW_MODIFY_ACCOUNTS);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_REMOVE_USER);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_USB_FILE_TRANSFER);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_SAFE_BOOT);

            showToast("Work profile restrictions set");
        } else {
            showToast("Not a profile owner");
        }

    }
    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}


