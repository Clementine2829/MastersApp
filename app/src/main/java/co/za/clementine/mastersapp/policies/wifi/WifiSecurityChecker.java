package co.za.clementine.mastersapp.policies.wifi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class WifiSecurityChecker {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private final Activity activity;
    private final WifiManager wifiManager;
    private final ConnectivityManager connectivityManager;

    public WifiSecurityChecker(Activity activity) {
        this.activity = activity;
        this.wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        this.connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void checkAndShowSecurityWarning() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            performSecurityCheck();
        }
    }

    private void performSecurityCheck() {
        if (!isCurrentNetworkSecure()) {
            showUnsecureNetworkDialog();
        }
    }

    private boolean isCurrentNetworkSecure() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        if (networkCapabilities == null || !networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return false;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult result : scanResults) {
            if (result.SSID.equals(ssid)) {
                return result.capabilities.contains("WPA") || result.capabilities.contains("WPA2") || result.capabilities.contains("WPA3") || result.capabilities.contains("WEP");
            }
        }
        return false;
    }

    private void showUnsecureNetworkDialog() {
        new AlertDialog.Builder(activity)
                .setTitle("Unsecure Network")
                .setMessage("You are connected to an unsecure network. Please connect to a secure network to continue using the app.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void handlePermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSecurityCheck();
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("Permission Required")
                        .setMessage("Location permission is required to check Wi-Fi security. Please grant this permission to proceed.")
                        .setPositiveButton("Try Again", (dialog, which) -> checkAndShowSecurityWarning())
                        .setNegativeButton("Exit", (dialog, which) -> {
                            activity.finish();
                            System.exit(0);
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }
}
