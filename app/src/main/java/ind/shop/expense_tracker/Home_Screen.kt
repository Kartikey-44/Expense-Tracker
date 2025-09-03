package ind.shop.expense_tracker

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import ind.shop.expense_tracker.databinding.ActivityHomeScreenBinding

class Home_Screen : BaseActivity() {
    lateinit var db: Expense_List_Database
    lateinit var adapter: MyAdapter
    lateinit var binding: ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Expense_List_Database(this)
        adapter = MyAdapter(this, db.get_all_expense().toMutableList())
        binding.transcationsList.layoutManager = LinearLayoutManager(this)
        binding.transcationsList.adapter = adapter



        // Search button

    }

    override fun onResume() {
        super.onResume()
        adapter.updateList(db.get_all_expense())
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val hasItems = (binding.transcationsList.adapter?.itemCount ?: 0) > 0
        binding.transcationsList.isVisible = hasItems
        binding.emptyLottie.isVisible = !hasItems
        if (hasItems) binding.emptyLottie.cancelAnimation() else binding.emptyLottie.playAnimation()
    }
}
