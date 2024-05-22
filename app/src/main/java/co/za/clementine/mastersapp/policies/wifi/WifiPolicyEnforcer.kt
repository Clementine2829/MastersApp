package co.za.clementine.mastersapp.policies.wifi

import android.content.Context
import androidx.appcompat.app.AlertDialog

class WifiPolicyEnforcer(private val context: Context, private val wifiPolicyManager: WifiPolicyManager) {

    fun enforceSecureWifiPolicy() {
        if (!wifiPolicyManager.isCurrentNetworkSecure()) {
            AlertDialog.Builder(context)
                .setTitle("Insecure Network")
                .setMessage("You are connected to an insecure network. Please switch to a secure network.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

//            wifiPolicyManager.suggestSecureNetwork("", "")
        }
    }
}
