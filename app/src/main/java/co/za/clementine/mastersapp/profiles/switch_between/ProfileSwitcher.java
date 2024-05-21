package co.za.clementine.mastersapp.profiles.switch_between;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class ProfileSwitcher {

    private final Context context;

    public ProfileSwitcher(Context context) {
        this.context = context;
    }

    public void switchToAdminProfile() {
        // Attempt to launch system settings (might not work on all devices)
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.Settings");

        if (context.getPackageManager().resolveActivity(intent, 0) == null) {

            intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            context.startActivity(intent);
        }
        context.startActivity(intent);
    }
}
