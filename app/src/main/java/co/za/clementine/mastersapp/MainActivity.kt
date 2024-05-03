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
import co.za.clementine.mastersapp.policies.device.DevicePolicies

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponentName: ComponentName
    private val PROVISIONING_REQUEST_CODE = 123
    val profileSelectionDialog = ProfileSelectionDialog(this)


    companion object {
        const val RESULT_ENABLE = 1
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEnableAdmin = findViewById<Button>(R.id.btnEnableAdmin)
        val btnDisableAdmin = findViewById<Button>(R.id.btnDisableAdmin)
        val btnLockTaskModeEnter = findViewById<Button>(R.id.btnLockTaskModeEnter)
        val btnLockTaskModeExit = findViewById<Button>(R.id.btnLockTaskModeExit)
        val btnSetProfileOwner = findViewById<Button>(R.id.btnSetProfileOwner)
        val btnNavigateToWorkProfile = findViewById<Button>(R.id.btnNavigateToWorkProfile)
        val btnGetProfile = findViewById<Button>(R.id.btnGetProfile)
        val btnLockDevice = findViewById<Button>(R.id.btnLockDevice)
        val btnSetPasswordPolicy = findViewById<Button>(R.id.btnSetPasswordPolicy)
        val btnSetWorkProfileRestrictions = findViewById<Button>(R.id.setWorkProfileRestrictions)

        checkDeviceOwner(savedInstanceState)


        val workProfileManager = WorkProfileManager(this)
        val devicePolicies = DevicePolicies(this)

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
            workProfileManager.createWorkProfile()
        }
        btnNavigateToWorkProfile.setOnClickListener {
            workProfileManager.navigateToWorkProfileSettings()
        }
        btnGetProfile.setOnClickListener {


            val user = profileSelectionDialog.show()
            Toast.makeText(this, "this " + user, Toast.LENGTH_SHORT).show()
            println("this userL " + user);
        }

        btnLockDevice.setOnClickListener {
            workProfileManager.lockProfile();
        }

        btnSetPasswordPolicy.setOnClickListener {
            devicePolicies.setPasswordPolicy()
        }

        btnSetWorkProfileRestrictions.setOnClickListener {
            devicePolicies.setWorkProfileRestrictions()
        }


    }


    private fun checkDeviceOwner(savedInstanceState: Bundle?) {

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponentName = ComponentName(this, DeviceOwnerReceiver::class.java)


        var doMessage = "App's device owner state is unknown"

        if (savedInstanceState == null) {
            val manager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            doMessage = if (manager.isDeviceOwnerApp(applicationContext.packageName)) {
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

        devicePolicyManager.setProfileName(adminComponentName, "Your Profile Name")
        Toast.makeText(this, "Profile owner enabled", Toast.LENGTH_SHORT).show()
    }
}
