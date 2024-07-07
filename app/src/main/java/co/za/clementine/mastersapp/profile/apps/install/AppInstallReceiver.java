package co.za.clementine.mastersapp.profile.apps.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import co.za.clementine.mastersapp.policies.device.ProfilePolicies;
import co.za.clementine.mastersapp.profile.apps.ManageWorkProfileInstalledApps;

public class AppInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            if (new ManageWorkProfileInstalledApps(context).isEndpointAirDroidInstalled()) {
                ProfilePolicies profilePolicies = new ProfilePolicies(context);
                profilePolicies.setWorkProfileRestrictions();
            }
        }
    }

}
