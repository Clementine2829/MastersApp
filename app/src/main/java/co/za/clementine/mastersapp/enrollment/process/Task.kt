package co.za.clementine.mastersapp.enrollment.process

data class Task(
    val name: String,
    var status: String,
    var retryVisible: Boolean = false,
    var undoVisible: Boolean = false,
    val action: suspend () -> Boolean,
    val undoAction: suspend () -> Boolean
)
