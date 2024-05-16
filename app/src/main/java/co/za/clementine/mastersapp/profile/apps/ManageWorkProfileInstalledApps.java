package co.za.clementine.mastersapp.profile.apps;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import java.util.ArrayList;
import java.util.List;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class ManageWorkProfileInstalledApps {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private Context context;

    public ManageWorkProfileInstalledApps(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(context, DeviceOwnerReceiver.class);
        this.context = context;
    }

    // Method to get a list of installed apps in the work profile
    public List<String> getInstalledAppsInWorkProfile() {
        List<String> installedApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            System.out.println(app.packageName);
            if ((app.flags & ApplicationInfo.FLAG_INSTALLED) != 0 /*&& (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0*/) {
                installedApps.add(app.packageName);
            }
        }

        return installedApps;
    }

    // Method to enable or disable an app in the work profile
    public void setAppEnabledInWorkProfile(String packageName, boolean enabled) {
        if (enabled) {
            devicePolicyManager.enableSystemApp(compName, packageName);
        } else {
            devicePolicyManager.setApplicationHidden(compName, packageName, true);
        }
    }

    // Method to check if work profile is available
    private boolean isWorkProfileAvailable() {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return userManager.isManagedProfile();
        }
        return false;
    }
}
