package co.za.clementine.mastersapp.enrollment.process

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.za.clementine.mastersapp.MainActivity
import co.za.clementine.mastersapp.R

class TaskAdapter(
    private val tasks: List<Task>,
    private val retryCallback: (Int) -> Unit,
    private val undoCallback: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name
        holder.taskStatus.text = task.status.toString()
        holder.taskStatus.setTextColor(
            if (task.status == MainActivity.TaskEnum.COMPLETED) holder.itemView.context.getColor(R.color.green)
            else holder.itemView.context.getColor(R.color.red)
        )
        holder.btnRetry.visibility = if (task.retryVisible) View.VISIBLE else View.GONE
        holder.btnUndo.visibility = if (task.undoVisible) View.VISIBLE else View.GONE
        holder.btnRetry.setOnClickListener { retryCallback(position) }
        holder.btnUndo.setOnClickListener { undoCallback(position) }
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.taskName)
        val taskStatus: TextView = view.findViewById(R.id.taskStatus)
        val btnRetry: TextView = view.findViewById(R.id.btnRetry)
        val btnUndo: TextView = view.findViewById(R.id.btnUndo)
    }
}
