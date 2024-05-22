package co.za.clementine.mastersapp

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import co.za.clementine.mastersapp.permissions.StorageAccessPermission
import co.za.clementine.mastersapp.policies.device.DevicePolicies
import co.za.clementine.mastersapp.policies.device.PoliciesManager
import co.za.clementine.mastersapp.policies.device.ProfilePolicies
import co.za.clementine.mastersapp.policies.wifi.PermissionManager
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyEnforcer
import co.za.clementine.mastersapp.policies.wifi.WifiPolicyManager
import co.za.clementine.mastersapp.profile.apps.ManageWorkProfileInstalledApps
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSelectionDialog
import co.za.clementine.mastersapp.profiles.switch_between.ProfileSwitcher


class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private val PROVISIONING_REQUEST_CODE = 123

    private lateinit var manageWorkProfileInstalledApps: ManageWorkProfileInstalledApps
    private lateinit var storageAccessPermission: StorageAccessPermission

    private lateinit var wifiPolicyManager: WifiPolicyManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var wifiPolicyEnforcer: WifiPolicyEnforcer



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
        val btnInstallApps = findViewById<Button>(R.id.btnInstallApps)
        val btnGetInstalledAppsInWorkProfile = findViewById<Button>(R.id.btnGetInstalledAppsInWorkProfile)
        val removeDeviceAdmin = findViewById<Button>(R.id.removeDeviceAdmin)
        val enforceWifiPolicies = findViewById<Button>(R.id.enforceWifiPolicies)


        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, DeviceOwnerReceiver::class.java)

        manageWorkProfileInstalledApps = ManageWorkProfileInstalledApps(this)

        wifiPolicyManager = WifiPolicyManager(this)
        permissionManager = PermissionManager(this)
        wifiPolicyEnforcer = WifiPolicyEnforcer(this, wifiPolicyManager)

        permissionManager.requestNecessaryPermissions()

        checkDeviceOwner(savedInstanceState)

        val workProfileManager = WorkProfileManager(this)

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
                val profileSwitcher = ProfileSwitcher(this);
                profileSwitcher.switchToAdminProfile();
//            } else {
//                showPolicyDialog(this)
//            }
        }

        btnLockDevice.setOnClickListener {
            workProfileManager.lockProfile();
        }

        btnSetDevicePolicy.setOnClickListener {
            DevicePolicies(this);
        }

        btnSetWorkProfileRestrictions.setOnClickListener {
            ProfilePolicies(this)
        }

        btnInstallApps.setOnClickListener {

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
            wifiPolicyEnforcer.enforceSecureWifiPolicy()
        }
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
}
