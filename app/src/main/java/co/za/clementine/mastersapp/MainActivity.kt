package co.za.clementine.mastersapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.network.AppSecurityManager
import co.za.clementine.mastersapp.network.NetworkMonitor
import co.za.clementine.mastersapp.permissions.StorageAccessPermission
import co.za.clementine.mastersapp.policies.bluetooth.BluetoothController
import co.za.clementine.mastersapp.policies.device.DevicePolicies
import co.za.clementine.mastersapp.policies.device.PoliciesManager
import co.za.clementine.mastersapp.policies.device.ProfilePolicies
import co.za.clementine.mastersapp.policies.wifi.PermissionManager
import co.za.clementine.mastersapp.policies.wifi.WifiBroadcastReceiver
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyEnforcer
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyManager
import co.za.clementine.mastersapp.profile.apps.ManageWorkProfileInstalledApps
import co.za.clementine.mastersapp.profile.apps.install.ApkInstaller
import co.za.clementine.mastersapp.profile.apps.install.PlayStoreInstaller
import co.za.clementine.mastersapp.profile.apps.install.WorkProfileAppInstaller
import co.za.clementine.mastersapp.profile.apps.install.WorkProfileAppInstaller1
import co.za.clementine.mastersapp.profile.apps.install.WorkProfileInstaller
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSelectionDialog
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSwitcher
import java.io.File
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), NetworkMonitor.NetworkStateListener  {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private val PROVISIONING_REQUEST_CODE = 123

    private lateinit var manageWorkProfileInstalledApps: ManageWorkProfileInstalledApps
    private lateinit var storageAccessPermission: StorageAccessPermission

    private lateinit var wifiPolicyManager: WifiPolicyManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var wifiPolicyEnforcer: WifiPolicyEnforcer
    private lateinit var wifiBroadcastReceiver: WifiBroadcastReceiver

    private var bluetoothController: BluetoothController? = null

    private lateinit var securityManager: AppSecurityManager
    private lateinit var networkMonitor: NetworkMonitor


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val profileSelectionDialog = ProfileSelectionDialog(this)

        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
        val btnDisableAdmin = findViewById<Button>(R.id.btnDisableAdmin)
        val btnLockTaskModeEnter = findViewById<Button>(R.id.btnLockTaskModeEnter)
        val btnLockTaskModeExit = findViewById<Button>(R.id.btnLockTaskModeExit)
        val btnSetProfileOwner = findViewById<Button>(R.id.btnSetProfileOwner)
        val btnNavigateToWorkProfile = findViewById<Button>(R.id.btnNavigateToWorkProfile)
        val btnGetProfile = findViewById<Button>(R.id.btnGetProfile)
        val btnSwitchToOwnerProfile = findViewById<Button>(R.id.btnSwitchToOwnerProfile)
        val btnLockDevice = findViewById<Button>(R.id.btnLockDevice)
        val btnSetDevicePolicy = findViewById<Button>(R.id.btnSetDevicePolicy)
        val btnSetWorkProfileRestrictions = findViewById<Button>(R.id.setWorkProfileRestrictions)
        val btnDownloadAirDroidApp = findViewById<Button>(R.id.btnDownloadAirDroidApp)
        val btnInstallApps = findViewById<Button>(R.id.btnInstallApps)
        val btnInstallAppsFromPLayStore = findViewById<Button>(R.id.btnInstallAppsFromPLayStore)
        val btnGetInstalledAppsInWorkProfile = findViewById<Button>(R.id.btnGetInstalledAppsInWorkProfile)
        val copyAppIntoWorkProfile = findViewById<Button>(R.id.copyAppIntoWorkProfile)
        val removeDeviceAdmin = findViewById<Button>(R.id.removeDeviceAdmin)
        val enforceWifiPolicies = findViewById<Button>(R.id.enforceWifiPolicies)
        val btnDisableDiscoverabilityButton = findViewById<Button>(R.id.btnDisableDiscoverabilityButton)
        val btnLimitConnectionsButton = findViewById<Button>(R.id.btnLimitConnectionsButton)


        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, DeviceOwnerReceiver::class.java)

        manageWorkProfileInstalledApps = ManageWorkProfileInstalledApps(this)

        wifiPolicyManager = WifiPolicyManager(this)
        permissionManager = PermissionManager(this)
        wifiPolicyEnforcer = WifiPolicyEnforcer(this, wifiPolicyManager)
        wifiBroadcastReceiver = WifiBroadcastReceiver(this)


        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, intentFilter)

        checkDeviceOwner(savedInstanceState)

        bluetoothController = BluetoothController(this)

        val workProfileManager = WorkProfileManager(this)

        securityManager = AppSecurityManager(this)
        networkMonitor = NetworkMonitor(this)
        networkMonitor.networkStateListener = this


        btnEnableAdmin.setOnClickListener {
            enableAdmin()
        }

        btnDisableAdmin.setOnClickListener {
            disableAdmin()
        }

        btnLockTaskModeEnter.setOnClickListener {
            lockTaskModeEnter()
        }

        btnLockTaskModeExit.setOnClickListener {
            lockTaskModeExit()
        }

        btnSetProfileOwner.setOnClickListener {
//            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
                workProfileManager.createWorkProfile()
//            } else {
//                showPolicyDialog(this)
//            }
        }
        btnNavigateToWorkProfile.setOnClickListener {
            workProfileManager.navigateToWorkProfileSettings()
        }
        btnGetProfile.setOnClickListener {
//            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
                profileSelectionDialog.showAndSwitchToWorkProfile()
//            } else {
//                showPolicyDialog(this)
//            }
        }

        btnSwitchToOwnerProfile.setOnClickListener {
//            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
                val profileSwitcher = ProfileSwitcher(this)
            profileSwitcher.switchToAdminProfile()
//            } else {
//                showPolicyDialog(this)
//            }
        }

        btnLockDevice.setOnClickListener {
            workProfileManager.lockProfile()
        }

        btnSetDevicePolicy.setOnClickListener {
            DevicePolicies(this)
        }

        btnSetWorkProfileRestrictions.setOnClickListener {
            ProfilePolicies(this)
        }

        btnDownloadAirDroidApp.setOnClickListener {

            val apkInstaller = ApkInstaller(this)
            val apkUrl = "https://dl.airdroid.com/AirDroid_4.3.7.1_airdroidhp.apk"
            apkInstaller.downloadAndInstall(apkUrl)

        }


        btnInstallApps.setOnClickListener {

//            val fileName = "downloaded_apk.apk"
            val fileName = "Whatsapp.apk"

            val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

            val workProfileInstaller = WorkProfileInstaller(this)
            workProfileInstaller.installApkInWorkProfile(apkFile)

        }

        btnInstallAppsFromPLayStore.setOnClickListener {
            val packageName = "com.whatsapp" // Replace with the package name of the app you want to install
            val playStoreInstaller = PlayStoreInstaller(this)
            playStoreInstaller.installAppFromPlayStore(packageName)
        }

        copyAppIntoWorkProfile.setOnClickListener {

            val packageName = "com.whatsapp" // Replace with the package name of the app you want to install in the work profile
            val workProfileAppInstaller1 = WorkProfileAppInstaller1(this)
            val workProfileAppInstaller = WorkProfileAppInstaller(this)
            workProfileAppInstaller.installAppInWorkProfile(packageName)

        }

        removeDeviceAdmin.setOnClickListener {
            PoliciesManager.removeDeviceAdmin(devicePolicyManager, adminComponentName, this);
        }
        btnGetInstalledAppsInWorkProfile.setOnClickListener {
            val workProfileApps = getWorkProfileInstalledApps()
            val adminApps = getAdminInstalledApps()
            println("Work profile installed apps ")
            workProfileApps.forEach{
                println(it)
            }
//            println("\n\nAdmin installed apps ")
//            adminApps.forEach{
//                println(it)
//            }
        }

        enforceWifiPolicies.setOnClickListener {
            permissionManager.requestNecessaryPermissions()
            wifiPolicyEnforcer.enforceSecureWifiPolicy()
        }

        btnDisableDiscoverabilityButton.setOnClickListener {
            bluetoothController!!.disableDiscoverability();
        }
        btnLimitConnectionsButton.setOnClickListener {
            bluetoothController!!.limitBluetoothConnections();
        }


        // Check and request necessary permissions at startup
        bluetoothController!!.checkAndRequestPermissions()

        if (!securityManager.isUserAuthenticated()) {
            securityManager.requestAuthentication(this)
        }

        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadcastReceiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        exitProcess(0)
    }
    private fun checkDeviceOwner(savedInstanceState: Bundle?) {
        var doMessage = "App's device owner state is unknown"

        if (savedInstanceState == null) {
            doMessage = if (devicePolicyManager.isDeviceOwnerApp(applicationContext.packageName)) {
                "App is device owner"
            } else {
                "App is not device owner"
            }
        }

        Log.e(TAG, doMessage)
        Toast.makeText(this, doMessage, Toast.LENGTH_SHORT).show()

    }
    private fun enableAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName)
        startActivity(intent)
    }
    private fun disableAdmin() {
        devicePolicyManager.removeActiveAdmin(adminComponentName)
    }
    private fun lockTaskModeEnter() {
        devicePolicyManager.setLockTaskPackages(adminComponentName, arrayOf(packageName))
        startLockTask()
    }
    private fun lockTaskModeExit() {
        stopLockTask()
    }

    private fun getWorkProfileInstalledApps(): List<String> {
        return manageWorkProfileInstalledApps.getInstalledAppsInWorkProfile()
    }

    private fun getAdminInstalledApps(): List<String> {
        return manageWorkProfileInstalledApps.getInstalledAppsForAdmin()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROVISIONING_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Provisioning successful, enable profile owner
                enableProfileOwner()
            } else {
                // Provisioning failed or canceled
                Toast.makeText(this, "Provisioning failed or canceled", Toast.LENGTH_SHORT).show()
            }
        }
        permissionManager.handleWriteSettingsResult(requestCode) {
            wifiPolicyEnforcer.enforceSecureWifiPolicy()
        }
        securityManager.handleAuthenticationResult(requestCode, resultCode)

        if (!securityManager.isUserAuthenticated()) {
            // Handle the case where authentication failed
            finish() // or take other appropriate action
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, grantResults) {
            wifiPolicyEnforcer.enforceSecureWifiPolicy()
        }
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                // Initialize BluetoothController or retry operations
            } else {
                // Permissions denied
                // Handle the case when permissions are not granted
            }
        }
    }
     private fun enableProfileOwner() {
        if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
            Toast.makeText(this, "App is not the device owner", Toast.LENGTH_SHORT).show()
            return
        }

        if (!devicePolicyManager.isProfileOwnerApp(packageName)) {
            Toast.makeText(this, "App is not the profile owner", Toast.LENGTH_SHORT).show()
            return
        }

        devicePolicyManager.setProfileName(adminComponentName, "Work Profile")
        Toast.makeText(this, "Profile owner enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (!securityManager.isUserAuthenticated()) {
            securityManager.requestAuthentication(this)
        }
        networkMonitor.registerNetworkCallback()
    }

    override fun onPause() {
        super.onPause()
        networkMonitor.unregisterNetworkCallback()
    }
    override fun onNetworkAvailable() {
        Toast.makeText(this, "Network is available", Toast.LENGTH_SHORT).show()
        // Proceed with the download or any network-dependent operations
    }

    override fun onNetworkLost() {
        Toast.makeText(this, "Network connection lost", Toast.LENGTH_SHORT).show()
        // Handle the loss of network connectivity
    }
}
