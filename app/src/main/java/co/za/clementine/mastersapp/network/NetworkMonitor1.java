package co.za.clementine.mastersapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkInfo;

public class NetworkMonitor1 {
    private Context context;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkStateListener networkStateListener;

    public NetworkMonitor1(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void setNetworkStateListener(NetworkStateListener listener) {
        this.networkStateListener = listener;
    }

    public boolean isNetworkAvailable() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void registerNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if (networkStateListener != null) {
                    networkStateListener.onNetworkAvailable();
                }
            }

            @Override
            public void onLost(Network network) {
                if (networkStateListener != null) {
                    networkStateListener.onNetworkLost();
                }
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void unregisterNetworkCallback() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public interface NetworkStateListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
}
