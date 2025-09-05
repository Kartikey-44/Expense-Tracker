package ind.shop.expense_tracker

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import ind.shop.expense_tracker.databinding.ActivitySplashBinding
import ind.shop.expense_tracker.databinding.FailureDialogBinding
import ind.shop.expense_tracker.databinding.PinDialogBinding
import ind.shop.expense_tracker.databinding.SuccessDialogBinding



class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val ui = Handler(Looper.getMainLooper())

    // Dialog references for safe dismissal
    private var pinDialog: Dialog? = null
    private var successDialog: Dialog? = null
    private var failureDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        val topCard = binding.topview
        val logo = binding.applogocontainer
        val taglineCard = binding.bottomview


        // Load animations
        val fadeIn: Animation? = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideTop: Animation? = AnimationUtils.loadAnimation(this, R.anim.slide_top)
        val slideBottom: Animation? = AnimationUtils.loadAnimation(this, R.anim.slide_bottom)


        topCard.startAnimation(slideTop)
        logo.startAnimation(fadeIn)
        taglineCard.startAnimation(slideBottom)

        val appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val firstOpen = appPrefs.getBoolean("firstopen", true)

        ui.postDelayed({
            if (firstOpen) {
                pin()
            } else {
                showPinDialog()
            }
        }, 3000)
    }

    override fun onPause() {
        super.onPause()
        // Safety: dismiss dialogs when leaving
        pinDialog?.dismiss(); pinDialog = null
        successDialog?.dismiss(); successDialog = null
        failureDialog?.dismiss(); failureDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Double safety
        pinDialog?.dismiss(); pinDialog = null
        successDialog?.dismiss(); successDialog = null
        failureDialog?.dismiss(); failureDialog = null
    }

    private fun showPinDialog() {
        var chances = 3
        val dialog = Dialog(this)
        val dialogBinding = PinDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(900, 1000)
        dialog.setCancelable(false)

        dialogBinding.setupin.text = "Enter The Pin"
        dialogBinding.instruction.text = "Enter your PIN. You have $chances chances."

        if (!isFinishing && !isDestroyed) dialog.show()
        pinDialog = dialog

        dialogBinding.confirmButton.setOnClickListener {
            val entered = dialogBinding.pinEdditext.text.toString().trim()
            val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val saved = prefs.getString("user_pin", null)

            if (entered.isNotEmpty() && entered == saved) {
                // Dismiss before navigation to avoid leaked window
                pinDialog?.dismiss(); pinDialog = null
                ui.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        startActivity(Intent(this, Home_Screen::class.java))
                        finish()
                    }
                }, 200)
            } else {
                chances--
                dialogBinding.instruction.text = "Incorrect PIN. $chances chances remaining."
                dialogBinding.pinEdditext.text?.clear()
                dialogBinding.pinEdditext.requestFocus()
                if (chances <= 0) {
                    pinDialog?.dismiss(); pinDialog = null
                    showFailure("App locked. Restart to try again.")
                    ui.postDelayed({ if (!isFinishing) finish() }, 1200)
                }
            }
        }
    }


    private fun success_dialog(message: String){

        val dialog= Dialog(this)
        val dialogBinding= SuccessDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700,800)
        dialogBinding.textonsucess.text="$message"
        dialog.show()
        dialog_dismiss(dialog)
        if(message=="Succcessfully Signed Up"){
            dialog.setOnDismissListener {
                pin()
            }
        }
        if(message== "Pin Confirmed"){
            dialog.setOnDismissListener {
                startActivity(Intent(applicationContext, Home_Screen::class.java))
            }
        }

    }
    private fun dialog_dismiss(dialog: Dialog){
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(dialog.isShowing){
                    dialog.dismiss()
                }
            },4000
        )

    }

    private fun fail_dialog(message: String){
        val dialog= Dialog(this)
        val dialogBinding= FailureDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700,600)
        dialogBinding.textonfailure.text="$message"
        dialog.show()
        dialog_dismiss(dialog)

    }



    private fun showFailure(message: String) {
        failureDialog?.dismiss()
        val d = Dialog(this)
        val b = FailureDialogBinding.inflate(layoutInflater)
        d.setContentView(b.root)
        d.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        d.window?.setLayout(700, 800)
        b.textonfailure.text = message
        if (!isFinishing && !isDestroyed) d.show()
        failureDialog = d
        ui.postDelayed({ if (d.isShowing) d.dismiss(); failureDialog = null }, 1200)
    }

    private fun pin(){
        val dialog= Dialog(this)

        val dialogBinding= PinDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(900,1000)
        dialog.show()
        dialogBinding.confirmButton.setOnClickListener {
            val pin=dialogBinding.pinEdditext.text.toString()
            if(pin.length<4 ){
                fail_dialog("Pin Must Be Four Characters Long At Least")
            }
            else{
                val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("user_pin", pin) // store as string
                editor.apply()
                val app_prefs=getSharedPreferences("app_prefs",MODE_PRIVATE)
                app_prefs.edit().putBoolean("firstopen",false).apply()
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        dialog.dismiss()
                        success_dialog("Pin Confirmed")
                    },500
                )


            }
        }
    }
}
