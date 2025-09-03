package ind.shop.expense_tracker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class MyAdapter(context: Context, private val expense: MutableList<Expense_Info>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paidto = itemView.findViewById<MaterialTextView>(R.id.paidto)
        val amount = itemView.findViewById<MaterialTextView>(R.id.amount)
        val category = itemView.findViewById<MaterialTextView>(R.id.category)
        val date = itemView.findViewById<MaterialTextView>(R.id.date)
        val remark = itemView.findViewById<MaterialTextView>(R.id.remark)
        val card = itemView.findViewById<MaterialCardView>(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val exp = expense[position]
        val formattedDate = dateFormat.format(java.util.Date(exp.date))
        holder.paidto.text = "Paid To :\t ${exp.paid_to}"

        val amountText = "Amount : \t \u20B9 ${exp.amount}"
        val spannable = SpannableString(amountText)
        val start = amountText.indexOf("\t")
        val end = start + "\u20B9 ${exp.amount}".length + 2
        if (start in 0 until amountText.length && end in 0..amountText.length && end > start) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#C62828")),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        holder.amount.text = spannable

        holder.category.text = "Category : \t${exp.category}"
        holder.date.text = "Date:\t$formattedDate"
        holder.remark.text = "Remark: \t${exp.remark}"

        holder.card.setOnClickListener {
            val p = holder.adapterPosition
            if (p == RecyclerView.NO_POSITION) return@setOnClickListener
            val t = expense[p]
            val intent = Intent(it.context, Add_Edit_Page::class.java).apply {
                putExtra(Add_Edit_Page.Action_Type, "view")
                putExtra(Add_Edit_Page.Expense_ID, t.id)
                putExtra(Add_Edit_Page.Expense_Paid_To, t.paid_to)
                putExtra(Add_Edit_Page.Amount, t.amount)
                putExtra(Add_Edit_Page.category, t.category)
                putExtra(Add_Edit_Page.date, t.date)
                putExtra(Add_Edit_Page.remark, t.remark)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = expense.size

    fun updateList(newList: List<Expense_Info>) {
        expense.clear()
        expense.addAll(newList)
        notifyDataSetChanged()
    }
}
