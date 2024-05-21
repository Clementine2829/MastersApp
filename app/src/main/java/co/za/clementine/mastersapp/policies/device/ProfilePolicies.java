package co.za.clementine.mastersapp.policies.device;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class ProfilePolicies extends PoliciesManager{
    public ProfilePolicies(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(context, DeviceOwnerReceiver.class);
        this.context = context;

        setWorkProfileRestrictions();
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
}
