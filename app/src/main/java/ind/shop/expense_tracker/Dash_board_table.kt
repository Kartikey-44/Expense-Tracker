import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ind.shop.expense_tracker.ExpenseItem
import ind.shop.expense_tracker.R

class Dash_board_table(private val expenseList: MutableList<ExpenseItem>) :
    RecyclerView.Adapter<Dash_board_table.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
        val categoryAmount: TextView = itemView.findViewById(R.id.category_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_table, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = expenseList[position]
        holder.categoryName.text = item.category
        holder.categoryAmount.text = "₹${"%.2f".format(item.amount)}"
    }

    override fun getItemCount() = expenseList.size

    fun updateList(newList: List<ExpenseItem>) {
        expenseList.clear()
        expenseList.addAll(newList)
        notifyDataSetChanged()
    }
}
