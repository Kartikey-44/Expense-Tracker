package ind.shop.expense_tracker

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import ind.shop.expense_tracker.databinding.ActivityCategoryPageBinding

class Category_Page : BaseActivity() {
    lateinit var binding: ActivityCategoryPageBinding
    lateinit var db: Expense_List_Database
    lateinit var adapter: MyAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityCategoryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val color = if (isNightMode) Color.WHITE else Color.BLACK


        val categoryadap=arrayOf("ALL","FOOD","TRAVEL","HOUSEHOLD")
        val categoryadapter= ArrayAdapter(this,R.layout.dropdown_item,categoryadap)
        binding.category.setAdapter(categoryadapter)

        val sort_by=arrayOf("Newest","Oldest","Amount","Low To High","High To Low")
        binding.sort.setAdapter(ArrayAdapter(this,R.layout.dropdown_item,sort_by))
        db= Expense_List_Database(this)
        binding.list.layoutManager= LinearLayoutManager(this)
        adapter= MyAdapter(this,db.get_all_expense().toMutableList())
        binding.list.adapter=adapter
        binding.category.setOnClickListener {
            binding.category.showDropDown()
            true
        }
        binding.sort.setOnClickListener {
            binding.sort.showDropDown()
            true
        }

        // Hold current selection
        var selectedCategory = "ALL"
        var selectedSort = "Newest" // or "NEWEST" depending on your DB logic

        binding.category.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = binding.category.adapter.getItem(position).toString()
            val resultList = db.filterAndSort(selectedCategory, selectedSort)
            binding.categoryLayout.isHintEnabled=false
            adapter.updateList(resultList)
        }

        binding.sort.setOnItemClickListener { _, _, position, _ ->
            selectedSort = binding.sort.adapter.getItem(position).toString()
            binding.sortLayout.isHintEnabled=false
            val resultList = db.filterAndSort(selectedCategory, selectedSort)
            adapter.updateList(resultList)
        }
        updateEmptyState()



    }

    private fun updateEmptyState() {
        val hasItems = (binding.list.adapter?.itemCount ?: 0) > 0
        binding.list.isVisible = hasItems
        binding.emptyLottie.isVisible = !hasItems
        if (hasItems) binding.emptyLottie.cancelAnimation() else binding.emptyLottie.playAnimation()
    }
}