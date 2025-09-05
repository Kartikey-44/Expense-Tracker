package ind.shop.expense_tracker

import Dash_board_table
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import ind.shop.expense_tracker.databinding.ActivityUserDashboardBinding
import kotlin.collections.iterator

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

        val minPercentToShow = 10f

        // Add only % values for slices
        for ((category, amount) in categoryTotals) {
            val percentage = if (totalExpense > 0) (amount / totalExpense) * 100 else 0f
            pieEntries.add(PieEntry(percentage, category))
        }

        val dataSet = PieDataSet(pieEntries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.HeadingColor))
        dataSet.valueTextSize = 14f
        dataSet.valueTypeface = ResourcesCompat.getFont(this, R.font.montserratbold)

        // Keep values INSIDE for large slices only
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE

        // Hide lines completely
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return if (value >= minPercentToShow) "${value.toInt()}%" else ""
            }
        }

        val pieData = PieData(dataSet)

        // Center text
        binding.chart.centerText = "Expenses Breakdown"
        binding.chart.setCenterTextColor(R.color.HeadingColor)
        binding.chart.setCenterTextSize(14f)
        binding.chart.setCenterTextTypeface(ResourcesCompat.getFont(this, R.font.montserratbold))

        // Legend - only category + percentage
        val legend = binding.chart.legend
        legend.isEnabled = true
        legend.textColor = ContextCompat.getColor(this, R.color.HeadingColor)
        legend.textSize = 12f
        legend.isWordWrapEnabled = true
        legend.form = Legend.LegendForm.CIRCLE

        val legendEntries = mutableListOf<LegendEntry>()
        for ((category, amount) in categoryTotals) {
            val percent = if (totalExpense > 0) (amount / totalExpense) * 100 else 0f
            val entry = LegendEntry()
            entry.label = "$category (${percent.toInt()}%)"
            entry.formColor = ColorTemplate.MATERIAL_COLORS[legendEntries.size % ColorTemplate.MATERIAL_COLORS.size]
            legendEntries.add(entry)
        }
        legend.setCustom(legendEntries)

        // Chart styling
        binding.chart.data = pieData
        binding.chart.description.isEnabled = false
        binding.chart.setUsePercentValues(false) // Already calculating percentages
        binding.chart.setDrawEntryLabels(false)
        binding.chart.setExtraOffsets(10f, 10f, 10f, 10f) // Padding so text stays inside
        binding.chart.animateY(1000)
        binding.chart.invalidate()
    }



}