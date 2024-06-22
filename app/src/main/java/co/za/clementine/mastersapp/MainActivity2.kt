package co.za.clementine.mastersapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.za.clementine.mastersapp.enrollment.process.Task
import co.za.clementine.mastersapp.enrollment.process.TaskAdapter
import kotlinx.coroutines.*

class MainActivity2 : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks, { position -> retryTask(position) }, { position -> undoTask(position) })
        recyclerView.adapter = adapter

        setupTasks()
        startTasks()
    }

    private fun setupTasks() {
        tasks.addAll(
            listOf(
//                Task("Enable Admin", MainActivity.TaskEnum.PENDING, action = ::enableAdmin, undoAction = ::undoEnableAdmin),
//                Task("Disable Admin", TaskEnum.PENDING, action = ::disableAdmin, undoAction = ::undoDisableAdmin),
//                Task("Lock Task Mode Enter", TaskEnum.PENDING, action = ::lockTaskModeEnter, undoAction = ::unlockTaskModeEnter),
//                Task("Lock Task Mode Exit", TaskEnum.PENDING, action = ::lockTaskModeExit, undoAction = ::unlockTaskModeExit),
//                Task("Set Profile Owner", TaskEnum.PENDING, action = ::setProfileOwner, undoAction = ::removeProfileOwner),
//                Task("Navigate to Work Profile", TaskEnum.PENDING, action = ::navigateToWorkProfile, undoAction = ::undoNavigateToWorkProfile),
//                Task("Get Profile", TaskEnum.PENDING, action = ::getProfile, undoAction = ::undoGetProfile),
//                Task("Switch to Owner Profile", TaskEnum.PENDING, action = ::switchToOwnerProfile, undoAction = ::undoSwitchToOwnerProfile),
//                Task("Lock Device", TaskEnum.PENDING, action = ::lockDevice, undoAction = ::unlockDevice),
//                Task("Set Device Policy", TaskEnum.PENDING, action = ::setDevicePolicy, undoAction = ::removeDevicePolicy),
//                Task("Set Work Profile Restrictions", TaskEnum.PENDING, action = ::setWorkProfileRestrictions, undoAction = ::removeWorkProfileRestrictions),
//                Task("Download AirDroid App", TaskEnum.PENDING, action = ::downloadAirDroidApp, undoAction = ::removeAirDroidApp),
//                Task("Install Apps", TaskEnum.PENDING, action = ::installApps, undoAction = ::uninstallApps),
//                Task("Get Installed Apps in Work Profile", TaskEnum.PENDING, action = ::getInstalledAppsInWorkProfile, undoAction = ::undoGetInstalledAppsInWorkProfile),
//                Task("Install Apps from Play Store", TaskEnum.PENDING, action = ::installAppsFromPlayStore, undoAction = ::uninstallAppsFromPlayStore),
//                Task("Copy App into Work Profile", TaskEnum.PENDING, action = ::copyAppIntoWorkProfile, undoAction = ::removeAppFromWorkProfile),
//                Task("Remove Device Admin", TaskEnum.PENDING, action = ::removeDeviceAdmin, undoAction = ::undoRemoveDeviceAdmin),
//                Task("Enforce Wi-Fi Policies", TaskEnum.PENDING, action = ::enforceWiFiPolicies, undoAction = ::removeWiFiPolicies),
//                Task("Disable Bluetooth Discoverability", TaskEnum.PENDING, action = ::disableBluetoothDiscoverability, undoAction = ::enableBluetoothDiscoverability),
//                Task("Limit Bluetooth Connections", TaskEnum.PENDING, action = ::limitBluetoothConnections, undoAction = ::removeBluetoothConnectionsLimit)
            )
        )
    }

    private fun startTasks() {
        lifecycleScope.launch {
            for (i in tasks.indices) {
                executeTask(i)
            }
        }
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
        tasks[position].status = MainActivity.TaskEnum.IN_PROGRESS
        tasks[position].retryVisible = false
        adapter.notifyItemChanged(position)

        // Auto scroll to the current task position
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            tasks[position].action()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            tasks[position].status = MainActivity.TaskEnum.COMPLETED
            tasks[position].undoVisible = true
        } else {
            tasks[position].status = MainActivity.TaskEnum.FAILED
            tasks[position].retryVisible = true
        }
        adapter.notifyItemChanged(position)
    }

    private suspend fun executeUndoTask(position: Int) {
        tasks[position].status = MainActivity.TaskEnum.UNDOING
        tasks[position].undoVisible = false
        adapter.notifyItemChanged(position)

        // Auto scroll to the current task position
        recyclerView.smoothScrollToPosition(position)

        val success = try {
            tasks[position].undoAction()
            true
        } catch (e: Exception) {
            false
        }

        if (success) {
            tasks[position].status = MainActivity.TaskEnum.UNDONE
        } else {
            tasks[position].status = MainActivity.TaskEnum.UNDO_FAILED
            tasks[position].undoVisible = true
        }
        adapter.notifyItemChanged(position)
    }

    // Example actions and undo actions
    private suspend fun enableAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoEnableAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun disableAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoDisableAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun lockTaskModeEnter(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun unlockTaskModeEnter(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun lockTaskModeExit(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun unlockTaskModeExit(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun setProfileOwner(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeProfileOwner(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun navigateToWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoNavigateToWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun getProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoGetProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun switchToOwnerProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoSwitchToOwnerProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun lockDevice(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun unlockDevice(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun setDevicePolicy(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeDevicePolicy(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun setWorkProfileRestrictions(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeWorkProfileRestrictions(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun downloadAirDroidApp(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeAirDroidApp(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun installApps(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun uninstallApps(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun getInstalledAppsInWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoGetInstalledAppsInWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun installAppsFromPlayStore(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun uninstallAppsFromPlayStore(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun copyAppIntoWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeAppFromWorkProfile(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeDeviceAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun undoRemoveDeviceAdmin(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun enforceWiFiPolicies(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeWiFiPolicies(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun disableBluetoothDiscoverability(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun enableBluetoothDiscoverability(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun limitBluetoothConnections(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }

    private suspend fun removeBluetoothConnectionsLimit(): Boolean {
        delay(2000)
        return (0..1).random() == 1
    }
}
