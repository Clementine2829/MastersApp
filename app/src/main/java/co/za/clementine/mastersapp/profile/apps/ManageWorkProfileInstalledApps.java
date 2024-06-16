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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import co.za.clementine.mastersapp.DeviceOwnerReceiver;

public class ManageWorkProfileInstalledApps {

    private final DevicePolicyManager devicePolicyManager;
    private final ComponentName adminComponent;
    private final Context context;

    public ManageWorkProfileInstalledApps(Context context,
                                          DevicePolicyManager devicePolicyManager,
                                          ComponentName adminComponent) {
        this.devicePolicyManager = devicePolicyManager;
        this.adminComponent = adminComponent;
        this.context = context;
    }

    public List<String> getInstalledAppsInWorkProfile() {
        List<String> installedApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        // Get the list of user profiles
        List<UserHandle> userHandles = userManager.getUserProfiles();

//        for (UserHandle userHandle : userHandles) {
//            // Check if this profile is a managed profile and the app is the profile owner
//            if (userManager.isManagedProfile(userHandle.getIdentifier()) && dpm.isProfileOwnerApp(adminComponent.getPackageName())) {
//                // Retrieve the installed applications for the work profile
//                Context workProfileContext = context.createPackageContextAsUser(context.getPackageName(), 0, userHandle);
//                PackageManager workProfilePackageManager = workProfileContext.getPackageManager();
//                List<ApplicationInfo> apps = workProfilePackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
//
//                for (ApplicationInfo app : apps) {
//                    if ((app.flags & ApplicationInfo.FLAG_INSTALLED) != 0) {
//                        installedApps.add(app.packageName);
//                    }
//                }
//            }
//        }

        return installedApps;
    }

    // Method to get a list of installed apps in the admin profile
    public List<String> getInstalledAppsForAdmin() {
        List<String> installedApps = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
//            System.out.println("App: " + app.packageName + ", Flags: " + app.flags);
            if ((app.flags & ApplicationInfo.FLAG_INSTALLED) != 0) {
//            if ((app.flags & ApplicationInfo.FLAG_INSTALLED) != 0 && (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                installedApps.add(app.packageName);
            }
        }

        return installedApps;
    }


    // Method to enable or disable an app in the work profile
    public void setAppEnabledInWorkProfile(String packageName, boolean enabled) {
        if (enabled) {
            devicePolicyManager.enableSystemApp(adminComponent, packageName);
        } else {
            devicePolicyManager.setApplicationHidden(adminComponent, packageName, true);
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
