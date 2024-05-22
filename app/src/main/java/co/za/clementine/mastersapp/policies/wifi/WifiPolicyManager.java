package co.za.clementine.mastersapp.policies.wifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

public class WifiPolicyManager {

    private final Context context;
    private final WifiManager wifiManager;

    public WifiPolicyManager(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isCurrentNetworkSecure() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, handle accordingly
            return false;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String currentSSID = wifiInfo.getSSID().replace("\"", "");  // Remove quotes

        List<ScanResult> scanResults = wifiManager.getScanResults();

        for (ScanResult scanResult : scanResults) {
            System.out.println("Clemy WiFi " + scanResult.toString());
            if (scanResult.SSID.equals(currentSSID)) {
                int securityType = getSecurityType(scanResult);
                System.out.println("Clemy securityType " + securityType);
                return securityType == SECURITY_WPA || securityType == SECURITY_WPA2 || securityType == SECURITY_WPA3;
            }
        }
        return false;
    }

    private int getSecurityType(ScanResult scanResult) {
        if (scanResult.capabilities.contains("WPA3")) {
            return SECURITY_WPA3;
        } else if (scanResult.capabilities.contains("WPA2")) {
            return SECURITY_WPA2;
        } else if (scanResult.capabilities.contains("WPA")) {
            return SECURITY_WPA;
        } else if (scanResult.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else {
            return SECURITY_OPEN;
        }
    }

    private static final int SECURITY_WPA3 = 3;
    private static final int SECURITY_WPA2 = 2;
    private static final int SECURITY_WPA = 1;
    private static final int SECURITY_WEP = 0;
    private static final int SECURITY_OPEN = -1;

    public void suggestSecureNetwork(String ssid, String password) {
        WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
            }
        });
    }
}
