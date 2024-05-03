package co.za.clementine.mastersapp.policies.device;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.widget.Toast;


import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class DevicePolicies  {


    DevicePolicyManager devicePolicyManager;
    ComponentName compName;
    Context context;
    public DevicePolicies(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(context, DeviceOwnerReceiver.class);
        this.context = context;
    }


    public void setPasswordPolicy() {

        assert devicePolicyManager != null;
        if (devicePolicyManager.isAdminActive(compName)) {
            // Set password quality to alphanumeric
            devicePolicyManager.setPasswordQuality(compName, DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);

            // Set minimum password length
            devicePolicyManager.setPasswordMinimumLength(compName, 8);

            // Set password expiration timeout
            devicePolicyManager.setPasswordExpirationTimeout(compName, 30L * 24L * 60L * 60L * 1000L);  // 30 days

            // Set maximum failed passwords for wipe
            devicePolicyManager.setMaximumFailedPasswordsForWipe(compName, 10);

            showToast("Password policy set");
        } else {
            showToast("Device Admin not active");
        }
    }

    public void setWorkProfileRestrictions() {

        if (devicePolicyManager.isProfileOwnerApp(context.getPackageName())) {
//            // Set some restrictions
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_SHARE_LOCATION);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_UNINSTALL_APPS);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_ADD_USER);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_CELL_BROADCASTS);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_BLUETOOTH);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_WIFI);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CREATE_WINDOWS);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_DEBUGGING_FEATURES);
//            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_FACTORY_RESET);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_INSTALL_APPS);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
            devicePolicyManager.addUserRestriction(compName, UserManager.DISALLOW_MODIFY_ACCOUNTS);
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


