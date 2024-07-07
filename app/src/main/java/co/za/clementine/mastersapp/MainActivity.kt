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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.za.clementine.mastersapp.Utils.Companion.confirmPopUpAction
import co.za.clementine.mastersapp.Utils.Companion.goToAboutUsActivity
import co.za.clementine.mastersapp.Utils.Companion.goToMainActivity
import co.za.clementine.mastersapp.Utils.Companion.goToMoreInfoActivity
import co.za.clementine.mastersapp.enrollment.process.Task
import co.za.clementine.mastersapp.enrollment.process.TaskAdapter
import co.za.clementine.mastersapp.exceptions.MyCustomException
import co.za.clementine.mastersapp.network.NetworkMonitor
import co.za.clementine.mastersapp.permissions.PermissionDialogManager
import co.za.clementine.mastersapp.permissions.StorageAccessPermission
import co.za.clementine.mastersapp.policies.bluetooth.BluetoothController
import co.za.clementine.mastersapp.policies.device.DevicePolicies
import co.za.clementine.mastersapp.policies.device.PoliciesManager.removeDeviceAdmin
import co.za.clementine.mastersapp.policies.device.PoliciesManager.showPolicyDialog
import co.za.clementine.mastersapp.policies.device.ProfilePolicies
import co.za.clementine.mastersapp.policies.wifi.PermissionManager
import co.za.clementine.mastersapp.policies.wifi.WifiBroadcastReceiver
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyEnforcer
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyManager
import co.za.clementine.mastersapp.profile.apps.ManageWorkProfileInstalledApps
import co.za.clementine.mastersapp.profile.apps.install.ApkInstaller
import co.za.clementine.mastersapp.profile.apps.install.AppInstallReceiver
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSelectionDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), NetworkMonitor.NetworkStateListener {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private val PROVISIONING_REQUEST_CODE = 123

    private lateinit var wifiPolicyManager: WifiPolicyManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var wifiPolicyEnforcer: WifiPolicyEnforcer
    private lateinit var wifiBroadcastReceiver: WifiBroadcastReceiver

    private var bluetoothController: BluetoothController? = null

    private lateinit var networkMonitor: NetworkMonitor

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: TaskAdapter

    private val tasks = mutableListOf<Task>()

    private lateinit var btnUndoAdmin: Button
    private lateinit var btnWorkProfile: Button

    private val delayTime: Long = 2000

    private lateinit var permissionDialogManager: PermissionDialogManager
    private lateinit var storageAccessPermission: StorageAccessPermission

    enum class TaskEnum {
        IN_PROGRESS,
        COMPLETED,
        PENDING,
        ENABLED,
        FAILED,
        UNDONE,
        UNDOING,
        UNDO_FAILED
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, DeviceOwnerReceiver::class.java)

        btnUndoAdmin = findViewById(R.id.btnUndoAdmin)
        btnWorkProfile = findViewById(R.id.btnWorkProfile)

        btnUndoAdmin.setOnClickListener {
            btnDisableAdmin()
        }
        btnWorkProfile.setOnClickListener {
            switchToWorkProfile()
        }

        wifiPolicyManager = WifiPolicyManager(this)
        permissionManager = PermissionManager(this)
        permissionManager.requestNecessaryPermissions()
        wifiPolicyEnforcer = WifiPolicyEnforcer(this, wifiPolicyManager)
        wifiBroadcastReceiver = WifiBroadcastReceiver(this)


        val intentFilter = IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, intentFilter)

        checkDeviceOwner(savedInstanceState)

        bluetoothController = BluetoothController(this)

        networkMonitor = NetworkMonitor(this)
        networkMonitor.networkStateListener = this

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            tasks,
            { position -> retryTask(position) },
            { position -> undoTask(position) })
        recyclerView.adapter = adapter

        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//            storageAccessPermission = StorageAccessPermission(this)
//            permissionDialogManager = PermissionDialogManager(this)
//            permissionDialogManager.showPermissionDialog()

            setupAdminTasks()
        } else if (devicePolicyManager.isProfileOwnerApp(packageName)) {
            setupWorkProfileTasks()
        } else {
            setupWorkProfileTasks()
        }
        startTasks()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_PACKAGE_ADDED)
        filter.addDataScheme("package")
        val appInstallReceiver = AppInstallReceiver()
        registerReceiver(appInstallReceiver, filter)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothController!!.checkAndRequestPermissions()
        }

        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val message = "Are you sure you want to exit? This action will close the app."
                confirmPopUpAction(this@MainActivity, "Exit App", message, { exitProcess(0) }, { })
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                gotToAboutUs()
                true
            }

            R.id.terms_and_conditions -> {
                gotToTermsAndConditions()
                true
            }

            R.id.refresh -> {
                relaunchMain()
                true
            }

            R.id.quit -> {
                val message = "Are you sure you want to quit? App will be closed."
                confirmAppExit(message)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun gotToAboutUs() {
        goToAboutUsActivity(this)
    }

    private fun gotToTermsAndConditions() {
        goToMoreInfoActivity(this)
    }

    private fun relaunchMain() {
        goToMainActivity(this)
    }

    private fun confirmAppExit(message: String) {
        val foo: () -> Unit = { enableAdmin2() }
        confirmPopUpAction(this, "Exit App", message, { exitProcess(0) }, foo)
    }

    private fun setupAdminTasks() {
        val devicePolicies = DevicePolicies(this)
        val workProfileManager = WorkProfileManager(this, devicePolicyManager, adminComponentName)
        tasks.addAll(
            listOf(
                Task(
                    name = "Enable Admin",
                    status = if (isAdminEnabled()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = ::enableAdmin,
                    undoAction = ::disableAdmin
                ),
                Task(
                    name = "Lock Task Mode Enter",
                    status = TaskEnum.PENDING,
                    action = ::lockTaskModeEnter,
                    undoAction = ::foo
                ),
                Task(
                    name = "Enforce Password policies",
                    status = if (devicePolicies.areSecurityPoliciesEnforced()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        devicePolicies.setPasswordSecurityPolicies()
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Enforce Storage Encryption policies",
                    status = if (devicePolicies.isStorageEncryptionEnforced()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        devicePolicies.enforceStorageEncryption()
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Enforce Screen Timeout policies",
                    status = if (devicePolicies.isScreenTimeoutEnforced) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        devicePolicies.setScreenTimeoutPolicy()
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Verify device policies",
                    status = if (devicePolicies.areSecurityPoliciesEnforced()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        devicePolicies.verifyPasswordPolicies(
                            devicePolicyManager,
                            adminComponentName
                        )
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Create work profile",
                    status = if (workProfileManager.workProfileExist()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        if (
                            devicePolicies.isPasswordSet &&
                            devicePolicies.isDevicePasswordSetAccordingToPolicies() &&
                            devicePolicies.isStorageEncryptionEnforced() &&
                            devicePolicies.isScreenTimeoutEnforced
                        ) {
                            if (workProfileManager.isMultipleUsersEnabled(this)) {
                                workProfileManager.createWorkProfile()
                                btnWorkProfile.visibility = View.VISIBLE
                            } else {
                                workProfileManager.showEnableMultipleUsersDialog()
                                println("isMultipleUsersEnabled? Not enabled")
                                throw MyCustomException(/*"security policy exception"*/)
                            }
                        } else {
                            showPolicyDialog(this)
                            throw MyCustomException(/*"security policy exception"*/)
                        }
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Exit Lock Task Mode Enter",
                    status = TaskEnum.PENDING,
                    action = ::lockTaskModeExit,
                    undoAction = ::foo
                ),
            )
        )

        if (isAdminEnabled()) {
            btnUndoAdmin.visibility = View.VISIBLE
        } else {
            btnUndoAdmin.visibility = View.GONE
        }
        if (workProfileManager.workProfileExist()) {
            btnWorkProfile.visibility = View.VISIBLE
        } else {
            btnWorkProfile.visibility = View.GONE
        }
    }

    private fun setupWorkProfileTasks() {
        val fileName = "AirDroid_4.3.7.1_airdroidhp.apk"
        val apkInstaller = ApkInstaller(this)
        tasks.addAll(
            listOf(
                Task(
                    name = "Download AirDroid App",
                    status = if (apkInstaller.fileExistsInDownloadDirectory(fileName)) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
//                        val apkUrl = "https://dl.airdroid.com/$fileName"
//                        val apkUrl = "https://airdroid.at/557530"
                        val apkUrl =
                            "https://s3.amazonaws.com/airtransfera/AirDroid_Business_Daemon_1.4.1.0_58705885_110298_sandstudio.apk"
                        apkInstaller.downloadAndInstall(apkUrl, packageManager)
                    },
                    undoAction = ::foo
                ),
                Task(
                    name = "Install AirDroid App",
                    status = if (ManageWorkProfileInstalledApps(this).isEndpointAirDroidInstalled()) TaskEnum.COMPLETED else TaskEnum.PENDING,
                    action = {
                        apkInstaller.installApk(apkInstaller.getAirDroidInDownloads())
                    },
                    undoAction = ::foo
                ),
                Task(name = "Enforce Work Profile Restrictions",
                    status = TaskEnum.PENDING,
                    action = {
//                        ProfilePolicies(this).setWorkProfileRestrictions()
                        throw MyCustomException()
                    }, undoAction = {
                        ProfilePolicies(this).setWorkProfileRestrictions()
                        throw MyCustomException()
                    }),
            )
        )
    }

    private fun foo() {}


    private fun startTasks() {
        lifecycleScope.launch {
            for (i in tasks.indices) {
                executeTask(i)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiBroadcastReceiver)
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
        println(tasks[position].toString())
        if (tasks[position].status == TaskEnum.COMPLETED) {
            adapter.notifyItemChanged(position)
            recyclerView.smoothScrollToPosition(position)
            return
        }
        tasks[position].status = TaskEnum.IN_PROGRESS
        tasks[position].retryVisible = false
        adapter.notifyItemChanged(position)
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            tasks[position].action()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            tasks[position].status = TaskEnum.COMPLETED
            tasks[position].undoVisible = false
            if (position == 0 && devicePolicyManager.isDeviceOwnerApp(packageName)) {
                tasks[position].undoVisible = true // this one is the enable admin thing
            }
        } else {
            tasks[position].status = TaskEnum.FAILED
            tasks[position].retryVisible = true
        }
        adapter.notifyItemChanged(position)
    }

    private suspend fun executeUndoTask(position: Int) {
        tasks[position].status = TaskEnum.UNDOING
        tasks[position].undoVisible = false
        adapter.notifyItemChanged(position)
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            tasks[position].undoAction()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            tasks[position].status = TaskEnum.UNDONE
        } else {
            tasks[position].status = TaskEnum.UNDO_FAILED
            tasks[position].undoVisible = true
        }
        adapter.notifyItemChanged(position)
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

    private suspend fun enableAdmin() {
        delay(delayTime)
        enableAdmin2()
    }

    private fun enableAdmin2() {
        if (!devicePolicyManager.isAdminActive(adminComponentName)) {
            AlertDialog.Builder(this)
                .setTitle("Enrollment process")
                .setMessage("The device will start with the enrollment process. Please allow the app to be an admin.\nCancel to stop and close the app.")
                .setPositiveButton("OK") { _, _ ->
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponentName)
                    startActivity(intent)
                }
                .setNeutralButton("Cancel") { _, _ ->
                    val message = "Are you sure you want to exit? Canceling will close the app."
                    confirmAppExit(message)
                }
                .create()
                .show()
        } else {
            println("Admin enabled already")
        }
    }

    private fun switchToWorkProfile() {
        val devicePolicies = DevicePolicies(this)
        if (
            devicePolicies.isPasswordSet &&
            devicePolicies.isDevicePasswordSetAccordingToPolicies() &&
            devicePolicies.isStorageEncryptionEnforced() &&
            devicePolicies.isScreenTimeoutEnforced
        ) {
            val message = "Do you want to switch to work profile?"
            confirmPopUpAction(
                this,
                "Work Profile",
                message,
                {
                    findViewById<Button>(R.id.btnWorkProfile).visibility = View.VISIBLE
                    ProfileSelectionDialog(
                        this,
                        devicePolicyManager,
                        adminComponentName
                    ).switchToProfile()
                },
                {}
            )
        } else {
            showPolicyDialog(this)
//            throw MyCustomException(/*"security policy exception"*/)
        }
    }

    private fun btnDisableAdmin() {
        val message = "Are you sure you want to disable admin privileges?"
        confirmPopUpAction(this, "Disable Admin", message, { disableAdmin() }, { })
    }

    private fun disableAdmin() {
        removeDeviceAdmin(devicePolicyManager, adminComponentName)
        devicePolicyManager.removeActiveAdmin(adminComponentName)
    }


    private fun isAdminEnabled(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponentName)
    }

    private suspend fun lockTaskModeEnter() {
        delay(delayTime)
        devicePolicyManager.setLockTaskPackages(adminComponentName, arrayOf(packageName))
        startLockTask()
    }

    private fun lockTaskModeExit() {
        stopLockTask()
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
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
        networkMonitor.registerNetworkCallback()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStart() {
        super.onStart()
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
//            storageAccessPermission = StorageAccessPermission(this)
//            permissionDialogManager = PermissionDialogManager(this)
//            storageAccessPermission.checkAndRequestPermission()
//            permissionDialogManager.showPermissionDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        networkMonitor.unregisterNetworkCallback()
    }

    override fun onNetworkAvailable() {
        Toast.makeText(this, "Network is available", Toast.LENGTH_SHORT).show()
    }

    override fun onNetworkLost() {
        AlertDialog.Builder(this)
            .setTitle("Network lost warning")
            .setMessage("Network connection lost. Please reconnect")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }
}
