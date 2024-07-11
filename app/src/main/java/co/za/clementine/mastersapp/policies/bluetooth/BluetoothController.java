package co.za.clementine.mastersapp.policies.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothController {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    public BluetoothController(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void disableDiscoverability() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkBluetoothPermissions()) {
                try {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    // BluetoothAdapter doesn't have setScanMode method directly available.
                    // We need to handle discoverability through intents and broadcasts if required.
                    // This can be more complex, but for simplicity, we'll assume Bluetooth is made non-discoverable through settings or via MDM.
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1);
        }
    }

    public void limitBluetoothConnections() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkBluetoothPermissions()) {
                try {
                    // Turn off Bluetooth to clear all connections
                    bluetoothAdapter.disable();
                    // Wait for Bluetooth to be disabled
                    while (bluetoothAdapter.isEnabled()) {
                        Thread.sleep(100);
                    }
                    // Turn on Bluetooth and limit it to essential connections
                    bluetoothAdapter.enable();

                    // Example: Connect to specific devices only
                    // Use a list of essential devices
                    String[] essentialDevices = {"00:11:22:33:AA:BB", "00:11:22:33:CC:DD"};
                    for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                        String deviceAddress = device.getAddress();
                        boolean isEssential = false;
                        for (String address : essentialDevices) {
                            if (deviceAddress.equals(address)) {
                                isEssential = true;
                                break;
                            }
                        }
                        if (!isEssential) {
                            // Remove non-essential device
                            try {
                                device.getClass().getMethod("removeBond", (Class[]) null).invoke(device, (Object[]) null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (SecurityException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private boolean checkBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1);
            return false;
        }
        return true;
    }
}
