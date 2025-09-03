package ind.shop.expense_tracker


import Dash_board_table
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import ind.shop.expense_tracker.R
import ind.shop.expense_tracker.databinding.ActivityUserDashboardBinding

class User_Dashboard : BaseActivity() {
    lateinit var binding: ActivityUserDashboardBinding
    lateinit var db: Expense_List_Database
    lateinit var adapter: Dash_board_table

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Expense_List_Database(this)
        setupChart()
        setupRecycler()
        loadData()
    }

    private fun setupRecycler() {
        adapter = Dash_board_table(mutableListOf())
        binding.expenseTable.layoutManager = LinearLayoutManager(this)
        binding.expenseTable.adapter = adapter
    }

    private fun loadData() {
        val categoryTotals = db.getCategoryTotals()
        val items = categoryTotals.map { (cat, amt) -> ExpenseItem(cat, amt) }
        adapter.updateList(items)
        binding.expenseTotalValue.text = "₹${"%.2f".format(db.getTotalAmount())}"
    }

    private fun setupChart() {
        val pieEntries = ArrayList<PieEntry>()
        val categoryTotals = db.getCategoryTotals()
        val totalExpense = db.getTotalAmount()

        for ((category, amount) in categoryTotals) {
            val percentage = if (totalExpense > 0) (amount / totalExpense) * 100 else 0f
            pieEntries.add(PieEntry(percentage, category))
        }

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.HeadingColor))
        val typefaceValue = ResourcesCompat.getFont(this, R.font.montserratbold)
        dataSet.valueTypeface = typefaceValue
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return "${value.toInt()}%"
            }
        }

        val pieData = PieData(dataSet)
        binding.chart.centerText = "Expenses Breakdown"
        binding.chart.setCenterTextColor(Color.BLACK)
        binding.chart.setCenterTextSize(18f)
        binding.chart.setCenterTextTypeface(typefaceValue)
        binding.chart.setDrawEntryLabels(true)
        binding.chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.TextColor))
        binding.chart.setEntryLabelTypeface(ResourcesCompat.getFont(this, R.font.robotobold))

        binding.chart.data = pieData
        binding.chart.description.isEnabled = false
        binding.chart.setUsePercentValues(true)
        binding.chart.animateY(1000)
        binding.chart.invalidate()
    }
}
