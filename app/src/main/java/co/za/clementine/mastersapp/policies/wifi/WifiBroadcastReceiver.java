package co.za.clementine.mastersapp.policies.wifi;

import static co.za.clementine.mastersapp.policies.wifi.PermissionManager.SECURITY_WPA;
import static co.za.clementine.mastersapp.policies.wifi.PermissionManager.SECURITY_WPA2;
import static co.za.clementine.mastersapp.policies.wifi.PermissionManager.SECURITY_WPA3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private final Context context;


    public WifiBroadcastReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null && wifiInfo.getSSID() != null) {
                if (!isCurrentNetworkSecure(wifiManager)) {
                    Toast.makeText(context, "Connected to an insecure Wi-Fi network: " + wifiInfo.getSSID(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private boolean isCurrentNetworkSecure(WifiManager wifiManager) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

        if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return isSecureNetwork(wifiInfo);
        }
        return false;
    }

    private boolean isSecureNetwork(WifiInfo wifiInfo) {
        // You can use other methods to determine the security of the network.
        // Here, we just check for WPA2 and WPA3.
        int securityType = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            securityType = wifiInfo.getCurrentSecurityType();
        }
//        int securityType = wifiInfo.getCurrentSecurityType();
//        return securityType == WifiInfo.SECURITY_TYPE_WPA2_PSK || securityType == WifiInfo.SECURITY_TYPE_WPA3_SAE;
        return securityType == SECURITY_WPA || securityType == SECURITY_WPA2 || securityType == SECURITY_WPA3;
    }
}
