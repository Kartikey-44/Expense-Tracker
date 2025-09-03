package ind.shop.expense_tracker

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import ind.shop.expense_tracker.databinding.ActivityAddEditPageBinding
import ind.shop.expense_tracker.databinding.FailureDialogBinding
import ind.shop.expense_tracker.databinding.PinDialogBinding
import ind.shop.expense_tracker.databinding.SuccessDialogBinding
import java.text.SimpleDateFormat
import java.util.*

class Add_Edit_Page : BaseActivity() {

    companion object {
        const val Action_Type = "Action_Type"
        const val Expense_ID = "id"
        const val Expense_Paid_To = "paid_to"
        const val Amount = "amount"
        const val category = "category"
        const val date = "date"
        const val remark = "remark"
    }

    lateinit var database: Expense_List_Database
    lateinit var binding: ActivityAddEditPageBinding

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        database = Expense_List_Database(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddEditPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        val action = intent.getStringExtra(Action_Type)
        when (action?.lowercase()) {
            "add" -> addExpense()
            "view" -> viewExpense()
        }


    }




    private fun addExpense() {
        binding.editButton.isVisible=false
        binding.delete.isVisible=false
        binding.addEdit.text = "ADD TRANSACTION HERE"
        binding.addButton.text = "Save"

        binding.addButton.setOnClickListener {
            val paidTo = binding.paidTo.text.toString().trim()
            val amountText = binding.amount.text.toString().trim()
            val categoryText = binding.category.text.toString().trim()
            val remarkText = if (binding.remark.text.toString().isEmpty()) {
                "No Remark"
            } else {
                binding.remark.text.toString()
            }

            if (paidTo.isEmpty() || categoryText.isEmpty() || amountText.isEmpty()) {
                failDialog("Failed.json", "Paid To, Amount, Category Cannot Be Empty")
                return@setOnClickListener
            }

            // Parse date string into timestamp (normalized to start of day)
            val dateMillis: Long = if (binding.date.text.isNullOrEmpty()) {
                System.currentTimeMillis()
            } else {
                // Convert dd/MM/yyyy string to Long
                try {
                    val dateString = binding.date.text.toString()
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = sdf.parse(dateString)
                    date?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
            checkPin { isCorrect ->
                if (isCorrect) {
                    database.add_expense(
                        Expense_Info(
                            0,
                            paidTo,
                            amountText,
                            categoryText,
                            dateMillis,
                            remarkText
                        )
                    )
                    successDialog("Expense Added")
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            startActivity(Intent(applicationContext, Home_Screen::class.java ))
                        },3000
                    )
                }
            }
        }
    }


    private fun dialogDismiss(dialog: Dialog) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, 4000)
    }

    private fun successDialog(message: String) {
        val dialog = Dialog(this)
        val dialogBinding = SuccessDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700, 800)
        dialogBinding.textonsucess.text = message
        dialog.show()
        dialogDismiss(dialog)
    }

    private fun failDialog(animation: String, message: String) {
        val dialog = Dialog(this)
        val dialogBinding = FailureDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.failureDialogLottie.setAnimation(animation)
        dialogBinding.failureDialogLottie.playAnimation()
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700, 800)
        dialogBinding.textonfailure.text = message
        dialog.show()
        dialogDismiss(dialog)
    }

    private fun checkPin(onResult: (Boolean) -> Unit) {
        val dialog = Dialog(this)
        val dialogBinding = PinDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(900, 1000)
        dialogBinding.setupin.text = "Enter The Pin"
        dialogBinding.instruction.text = "Enter The Pin To Confirm The Action"

        dialogBinding.confirmButton.setOnClickListener {
            val pin = dialogBinding.pinEdditext.text.toString()
            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val pined = prefs.getString("user_pin", null)

            if (pin.isEmpty() || pin.trim() != pined?.trim()) {
                failDialog("Failed.json", "Pin Not Matched")
            } else {
                onResult(true)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun viewExpense() {
        binding.editButton.isVisible=true
        binding.delete.isVisible=true
        binding.addEdit.text = "Your Transaction"
        binding.addButton.text="Viewed"
        val paidTo = intent.getStringExtra(Expense_Paid_To) ?: ""
        val amount = intent.getStringExtra(Amount) ?: ""
        val categoryText = intent.getStringExtra(category) ?: ""
        val dateMillis = intent.getLongExtra(date, System.currentTimeMillis())
        val remarkText = intent.getStringExtra(remark) ?: ""

        binding.paidTo.setText(paidTo)
        binding.amount.setText(amount)
        binding.category.setText(categoryText)
        binding.date.setText(dateFormat.format(Date(dateMillis)))
        binding.remark.setText(remarkText)

        binding.paidTo.isEnabled = false
        binding.amount.isEnabled = false
        binding.category.isEnabled = false
        binding.date.isEnabled = false
        binding.remark.isEnabled = false
        binding.addButton.setOnClickListener {
            finish()
        }

        binding.editButton.setOnClickListener { editExpense() }
        binding.delete.setOnClickListener { deleteExpense() }
    }

    private fun editExpense() {
        binding.editButton.textSize = 12f
        binding.editButton.text = "Confirm"
        binding.paidTo.isEnabled = true
        binding.amount.isEnabled = true
        binding.category.isEnabled = true
        binding.date.isEnabled = true
        binding.remark.isEnabled = true

        binding.editButton.setOnClickListener {
            if (binding.paidTo.text.toString().isEmpty() ||
                binding.amount.text.toString().isEmpty() ||
                binding.category.text.toString().isEmpty()
            ) {
                failDialog("Failed.json", "Any Field Cannot Be Empty")
            } else {
                checkPin { isCorrect ->
                    if (isCorrect) {
                        val id = intent.getIntExtra(Expense_ID, -1)
                        val paid = binding.paidTo.text.toString()
                        val amount = binding.amount.text.toString()
                        val categoryText = binding.category.text.toString()
                        val dateMillis = try {
                            val dateString = binding.date.text.toString()
                            val parsedDate = dateFormat.parse(dateString)
                            parsedDate?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val remarkText = binding.remark.text.toString()

                        val db = Expense_List_Database(this)
                        db.edit_task(
                            Expense_Info(
                                id,
                                paid,
                                amount,
                                categoryText,
                                dateMillis,
                                remarkText
                            )
                        )
                        successDialog("Task Is Edited")
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(Intent(applicationContext, Home_Screen::class.java))
                        }, 3000)
                    }
                }
            }
        }
    }

    private fun deleteExpense() {
        checkPin { isCorrect ->
            if (isCorrect) {
                val db = Expense_List_Database(this)
                val id = intent.getIntExtra(Expense_ID, -1)
                if (id != -1) {
                    db.delete_expense(id)
                    failDialog("DeleteBin.json", "Expense Deleted")
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            startActivity(Intent(applicationContext, Home_Screen::class.java))
                        },3000
                    )

                } else {
                    failDialog("Failed.json", "Missing Expense ID")
                }
            }
        }
    }
}
