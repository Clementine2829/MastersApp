package co.za.clementine.mastersapp.enrollment.process

import kotlin.reflect.KFunction0

data class Task(
    val name: String,
    var status: String,
    var retryVisible: Boolean = false,
    var undoVisible: Boolean = false,
    val action: suspend () -> Unit,
    val undoAction: suspend () -> Unit
)
