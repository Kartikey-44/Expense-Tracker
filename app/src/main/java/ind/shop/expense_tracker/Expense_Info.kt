package ind.shop.expense_tracker


data class Expense_Info(val id: Int,val paid_to: String,
                        val amount: String, val category: String,
                        val date: Long, val remark: String)
