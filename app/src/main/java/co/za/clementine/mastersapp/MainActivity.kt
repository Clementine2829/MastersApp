package co.za.clementine.mastersapp

import android.app.AlertDialog
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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.za.clementine.mastersapp.enrollment.process.Task
import co.za.clementine.mastersapp.enrollment.process.TaskAdapter
import co.za.clementine.mastersapp.network.AppSecurityManager
import co.za.clementine.mastersapp.network.NetworkMonitor
import co.za.clementine.mastersapp.permissions.StorageAccessPermission
import co.za.clementine.mastersapp.policies.bluetooth.BluetoothController
import co.za.clementine.mastersapp.policies.device.DevicePolicies
import co.za.clementine.mastersapp.policies.wifi.PermissionManager
import co.za.clementine.mastersapp.policies.wifi.WifiBroadcastReceiver
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyEnforcer
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyManager
import co.za.clementine.mastersapp.profile.apps.ManageWorkProfileInstalledApps
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSelectionDialog
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSwitcher
import kotlinx.coroutines.launch
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var adminProfileAdapter: TaskAdapter
    private lateinit var workProfileAdapter: TaskAdapter
    private val adminProfileTasks = mutableListOf<Task>()
    private val workProfileTasks = mutableListOf<Task>()


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        val profileSelectionDialog = ProfileSelectionDialog(this)

//        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
//        val btnDisableAdmin = findViewById<Button>(R.id.btnDisableAdmin)
//        val btnLockTaskModeEnter = findViewById<Button>(R.id.btnLockTaskModeEnter)
//        val btnLockTaskModeExit = findViewById<Button>(R.id.btnLockTaskModeExit)
//        val btnSetProfileOwner = findViewById<Button>(R.id.btnSetProfileOwner)
//        val btnNavigateToWorkProfile = findViewById<Button>(R.id.btnNavigateToWorkProfile)
//        val btnGetProfile = findViewById<Button>(R.id.btnGetProfile)
//        val btnSwitchToOwnerProfile = findViewById<Button>(R.id.btnSwitchToOwnerProfile)
//        val btnLockDevice = findViewById<Button>(R.id.btnLockDevice)
//        val btnSetDevicePolicy = findViewById<Button>(R.id.btnSetDevicePolicy)
//        val btnSetWorkProfileRestrictions = findViewById<Button>(R.id.setWorkProfileRestrictions)
//        val btnDownloadAirDroidApp = findViewById<Button>(R.id.btnDownloadAirDroidApp)
//        val btnInstallApps = findViewById<Button>(R.id.btnInstallApps)
//        val btnInstallAppsFromPLayStore = findViewById<Button>(R.id.btnInstallAppsFromPLayStore)
//        val btnGetInstalledAppsInWorkProfile = findViewById<Button>(R.id.btnGetInstalledAppsInWorkProfile)
//        val copyAppIntoWorkProfile = findViewById<Button>(R.id.copyAppIntoWorkProfile)
//        val removeDeviceAdmin = findViewById<Button>(R.id.removeDeviceAdmin)
//        val enforceWifiPolicies = findViewById<Button>(R.id.enforceWifiPolicies)
//        val btnDisableDiscoverabilityButton = findViewById<Button>(R.id.btnDisableDiscoverabilityButton)
//        val btnLimitConnectionsButton = findViewById<Button>(R.id.btnLimitConnectionsButton)


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


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adminProfileAdapter = TaskAdapter(adminProfileTasks, { position -> retryTask(position) }, { position -> undoTask(position) })
        recyclerView.adapter = adminProfileAdapter

        setupAdminTasks(this)
        startAdminTasks()

//
//        btnEnableAdmin.setOnClickListener {
//            enableAdmin()
//        }
//
//        btnDisableAdmin.setOnClickListener {
//            disableAdmin()
//        }
//
//        btnLockTaskModeEnter.setOnClickListener {
//            lockTaskModeEnter()
//        }
//
//        btnLockTaskModeExit.setOnClickListener {
//            lockTaskModeExit()
//        }
//
//        btnSetProfileOwner.setOnClickListener {
////            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
//                workProfileManager.createWorkProfile()
////            } else {
////                showPolicyDialog(this)
////            }
//        }
//        btnNavigateToWorkProfile.setOnClickListener {
//            workProfileManager.navigateToWorkProfileSettings()
//        }
//        btnGetProfile.setOnClickListener {
////            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
//                profileSelectionDialog.showAndSwitchToWorkProfile()
////            } else {
////                showPolicyDialog(this)
////            }
//        }
//
//        btnSwitchToOwnerProfile.setOnClickListener {
////            if (arePoliciesApplied(devicePolicyManager, adminComponentName)){
//                val profileSwitcher = ProfileSwitcher(this)
//            profileSwitcher.switchToAdminProfile()
////            } else {
////                showPolicyDialog(this)
////            }
//        }
//
//        btnLockDevice.setOnClickListener {
//            workProfileManager.lockProfile()
//        }
//
//        btnSetDevicePolicy.setOnClickListener {
//            DevicePolicies(this)
//        }
//
//        btnSetWorkProfileRestrictions.setOnClickListener {
//            ProfilePolicies(this)
//        }
//
//        btnDownloadAirDroidApp.setOnClickListener {
//
//            val apkInstaller = ApkInstaller(this)
//            val apkUrl = "https://dl.airdroid.com/AirDroid_4.3.7.1_airdroidhp.apk"
//            apkInstaller.downloadAndInstall(apkUrl)
//
//        }
//
//
//        btnInstallApps.setOnClickListener {
//
////            val fileName = "downloaded_apk.apk"
//            val fileName = "Whatsapp.apk"
//
//            val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
//
//            val workProfileInstaller = WorkProfileInstaller(this)
//            workProfileInstaller.installApkInWorkProfile(apkFile)
//
//        }
//
//        btnInstallAppsFromPLayStore.setOnClickListener {
//            val packageName = "com.whatsapp" // Replace with the package name of the app you want to install
//            val playStoreInstaller = PlayStoreInstaller(this)
//            playStoreInstaller.installAppFromPlayStore(packageName)
//        }
//
//        copyAppIntoWorkProfile.setOnClickListener {
//
//            val packageName = "com.whatsapp" // Replace with the package name of the app you want to install in the work profile
//            val workProfileAppInstaller1 = WorkProfileAppInstaller1(this)
//            val workProfileAppInstaller = WorkProfileAppInstaller(this)
//            workProfileAppInstaller.installAppInWorkProfile(packageName)
//
//        }
//
//        removeDeviceAdmin.setOnClickListener {
//            PoliciesManager.removeDeviceAdmin(devicePolicyManager, adminComponentName, this);
//        }
//        btnGetInstalledAppsInWorkProfile.setOnClickListener {
//            val workProfileApps = getWorkProfileInstalledApps()
//            val adminApps = getAdminInstalledApps()
//            println("Work profile installed apps ")
//            workProfileApps.forEach{
//                println(it)
//            }
////            println("\n\nAdmin installed apps ")
////            adminApps.forEach{
////                println(it)
////            }
//        }
//
//        enforceWifiPolicies.setOnClickListener {
//            permissionManager.requestNecessaryPermissions()
//            wifiPolicyEnforcer.enforceSecureWifiPolicy()
//        }
//
//        btnDisableDiscoverabilityButton.setOnClickListener {
//            bluetoothController!!.disableDiscoverability();
//        }
//        btnLimitConnectionsButton.setOnClickListener {
//            bluetoothController!!.limitBluetoothConnections();
//        }


        // Check and request necessary permissions at startup
        bluetoothController!!.checkAndRequestPermissions()

        if (!securityManager.isUserAuthenticated()) {
            securityManager.requestAuthentication(this)
        }

        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
        }

    }

    private fun setupAdminTasks(context: Context) {
        adminProfileTasks.addAll(
            listOf(
                Task(
                    name = "Enable Admin",
                    status = if (isAdminEnabled()) "Enabled" else "Pending",
                    action = ::enableAdmin,
                    undoAction = ::disableAdmin),
//                Task(
//                    name = "Lock Task Mode Enter",
//                    status = "Pending",
//                    action = ::lockTaskModeEnter,
//                    undoAction = ::lockTaskModeExit),
//                Task("Lock Device", "Pending", action = ::lockDevice, undoAction = ::unlockDevice),
//                Task(name = "Set Device Policy", status = "Pending", action = ::DevicePolicies(), undoAction = ::removeDevicePolicy),
//                Task(name = "Create work profile",
//                    status = "Pending",
//                    action = ::setProfileOwner, undoAction = ::removeProfileOwner),
//                Task(name = "Switch to work profile",
//                    status = "Pending",
//                    action = ::navigateToWorkProfile, undoAction = ::undoNavigateToWorkProfile),
//                Task(name = "Enforce Wi-Fi Policies",
//                    status = "Pending",
//                    action = ::enforceWiFiPolicies,
//                    undoAction = ::removeWiFiPolicies),
//                Task(name = "Enforce Bluetooth policies",
//                    status = "Pending",
//                    action = ::disableBluetoothDiscoverability,
//                    undoAction = ::enableBluetoothDiscoverability),
                Task(
                    name = "Enforce Password policies",
                    status = "Pending",
                    action = {
                        val devicePolicies = DevicePolicies(context)
                        devicePolicies.setPasswordSecurityPolicies()
                    },
                    undoAction = ::foo),
                Task(
                    name = "Enforce Storage Encryption policies",
                    status = "Pending",
                    action = {
                    val devicePolicies = DevicePolicies(context)
                    devicePolicies.enforceStorageEncryption()
                },
                    undoAction = ::foo),
                Task(
                    name = "Enforce Screen Timeout policies",
                    status = "Pending",
                    action = {
                        val devicePolicies = DevicePolicies(context)
                        devicePolicies.setScreenTimeoutPolicy(10000) // 10 seconds
//                        devicePolicies.setScreenTimeoutPolicy(3000) // 3 seconds
                    },
                    undoAction = ::foo),
//                Task(name = "Enforce Network policies",
//                    status = "Pending",
//                    action = {
//                        val devicePolicies = DevicePolicies(context)
//                        devicePolicies.setPasswordSecurityPolicies()
//                    }, undoAction = ::foo),
            )
        )
    }

    private fun foo(){}
    private fun setupWorProfileTasks() {
        adminProfileTasks.addAll(
            listOf(
//                Task("Enable Admin", "Pending", action = ::enableAdmin, undoAction = ::undoEnableAdmin),
//                Task("Lock Task Mode Enter", "Pending", action = ::lockTaskModeEnter, undoAction = ::unlockTaskModeEnter),
//                Task("Lock Task Mode Exit", "Pending", action = ::lockTaskModeExit, undoAction = ::unlockTaskModeExit),
//                Task("Set Profile Owner", "Pending", action = ::setProfileOwner, undoAction = ::removeProfileOwner),
//                Task("Navigate to Work Profile", "Pending", action = ::navigateToWorkProfile, undoAction = ::undoNavigateToWorkProfile),
//                Task("Get Profile", "Pending", action = ::getProfile, undoAction = ::undoGetProfile),
//                Task("Switch to Owner Profile", "Pending", action = ::switchToOwnerProfile, undoAction = ::undoSwitchToOwnerProfile),
//                Task("Lock Device", "Pending", action = ::lockDevice, undoAction = ::unlockDevice),
//                Task("Set Device Policy", "Pending", action = ::setDevicePolicy, undoAction = ::removeDevicePolicy),
//                Task("Set Work Profile Restrictions", "Pending", action = ::setWorkProfileRestrictions, undoAction = ::removeWorkProfileRestrictions),
//                Task("Download AirDroid App", "Pending", action = ::downloadAirDroidApp, undoAction = ::removeAirDroidApp),
//                Task("Install Apps", "Pending", action = ::installApps, undoAction = ::uninstallApps),
//                Task("Get Installed Apps in Work Profile", "Pending", action = ::getInstalledAppsInWorkProfile, undoAction = ::undoGetInstalledAppsInWorkProfile),
//                Task("Install Apps from Play Store", "Pending", action = ::installAppsFromPlayStore, undoAction = ::uninstallAppsFromPlayStore),
//                Task("Copy App into Work Profile", "Pending", action = ::copyAppIntoWorkProfile, undoAction = ::removeAppFromWorkProfile),
//                Task("Remove Device Admin", "Pending", action = ::removeDeviceAdmin, undoAction = ::undoRemoveDeviceAdmin),
//                Task("Enforce Wi-Fi Policies", "Pending", action = ::enforceWiFiPolicies, undoAction = ::removeWiFiPolicies),
//                Task("Disable Bluetooth Discoverability", "Pending", action = ::disableBluetoothDiscoverability, undoAction = ::enableBluetoothDiscoverability),
//                Task("Limit Bluetooth Connections", "Pending", action = ::limitBluetoothConnections, undoAction = ::removeBluetoothConnectionsLimit)
            )
        )
    }


    private fun startAdminTasks() {
        lifecycleScope.launch {
            for (i in adminProfileTasks.indices) {
                executeTask(i)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadcastReceiver)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        exitProcess(0)
    }


    private fun retryTask(position: Int) {
        lifecycleScope.launch {
            executeTask(position)
        }
    }

    private fun undoTask(position: Int) {
        lifecycleScope.launch {
            executeUndoTask(position)
        }
    }

    private suspend fun executeTask(position: Int) {
        adminProfileTasks[position].status = "In Progress"
        adminProfileTasks[position].retryVisible = false
        adminProfileAdapter.notifyItemChanged(position)

        // Auto scroll to the current task position
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            adminProfileTasks[position].action()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            adminProfileTasks[position].status = "Completed"
            adminProfileTasks[position].undoVisible = true
        } else {
            adminProfileTasks[position].status = "Failed"
            adminProfileTasks[position].retryVisible = true
        }
        adminProfileAdapter.notifyItemChanged(position)
    }

    private suspend fun executeUndoTask(position: Int) {
        adminProfileTasks[position].status = "Undoing"
        adminProfileTasks[position].undoVisible = false
        adminProfileAdapter.notifyItemChanged(position)

        // Auto scroll to the current task position
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            adminProfileTasks[position].undoAction()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            adminProfileTasks[position].status = "Undone"
        } else {
            adminProfileTasks[position].status = "Undo Failed"
            adminProfileTasks[position].undoVisible = true
        }
        adminProfileAdapter.notifyItemChanged(position)
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
        if(!isAdminEnabled()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enrollment process")
            builder.setMessage("The device will start with enrollment process. Please allow app to be an admin \nCancel to stop and close app")
            builder.setPositiveButton("OK") { _, _ ->
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName)
                startActivity(intent)
            }
            builder.setNeutralButton("Cancel") { _, _ ->
                finishAffinity()
                exitProcess(0)
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
    private fun disableAdmin() {
        devicePolicyManager.removeActiveAdmin(adminComponentName)
    }
    private fun isAdminEnabled(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return devicePolicyManager.isAdminActive(adminComponentName)
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
