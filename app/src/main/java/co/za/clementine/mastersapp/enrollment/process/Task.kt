package co.za.clementine.mastersapp.enrollment.process

import co.za.clementine.mastersapp.MainActivity
import kotlin.reflect.KFunction0

data class Task(
    val name: String,
    var status: MainActivity.TaskEnum,
    var retryVisible: Boolean = false,
    var undoVisible: Boolean = false,
    val action: suspend () -> Unit,
    val undoAction: suspend () -> Unit
) {
    override fun toString(): String {
        return "Task(name='$name', status='$status', retryVisible=$retryVisible, undoVisible=$undoVisible, action=$action, undoAction=$undoAction)"
    }
}
