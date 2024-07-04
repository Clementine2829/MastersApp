package co.za.clementine.mastersapp.permissions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

public class PermissionDialogManager {

    private final Context context;

    public PermissionDialogManager(Context context) {
        this.context = context;
    }

    public void showPermissionDialog() {
        if (!Settings.System.canWrite(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Permission Required")
                    .setMessage("The app needs to modify system settings. Do you want to proceed?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();
                        openAppSettings();
                    })
                    .setNeutralButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        showExitMessage();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        ((Activity) context).startActivityForResult(intent, 1);
    }

    private void showExitMessage() {
        Toast.makeText(context, "You cannot continue without granting permission. The app will close now.", Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Permission Denied")
            .setMessage("Without the required permission, the app cannot function properly. Please grant the permission to continue using the app.")
            .setPositiveButton("TRY AGAIN", (dialog, which) -> {
                dialog.dismiss();
                showPermissionDialog();
            })
            .setNeutralButton("CLOSE APP", (dialog, which) -> {
                dialog.dismiss();
                ((Activity) context).finishAffinity();
            })
            .setCancelable(false)
            .show();
    }
}
