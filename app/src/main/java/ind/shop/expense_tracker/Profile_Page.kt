package ind.shop.expense_tracker

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import ind.shop.expense_tracker.databinding.ActivityProfilePageBinding
import ind.shop.expense_tracker.databinding.ConfirmationDialogBinding
import ind.shop.expense_tracker.databinding.FailureDialogBinding
import ind.shop.expense_tracker.databinding.LoadingDialogBinding
import ind.shop.expense_tracker.databinding.PinDialogBinding
import ind.shop.expense_tracker.databinding.SuccessDialogBinding
import ind.shop.expense_tracker.databinding.UsernameDialogBinding

class Profile_Page : BaseActivity() {

    companion object {
        const val PREFS_SETTINGS = "settings"
        const val PREFS_APP = "app_prefs"
        const val PREFS_USERNAME = "username"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_FIRST_OPEN = "firstopen"
        const val KEY_USER_PIN_FILE = "MyAppPrefs"
        const val KEY_USER_PIN = "user_pin"
        const val KEY_USER_NAME = "user_name"

        // Firebase node
        const val USERS_NODE = "Users"
        const val PASSWORD_FIELD = "password" // change to "pass" if your DB uses that key
    }

    lateinit var db: Expense_List_Database
    lateinit var binding: ActivityProfilePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge first
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Install compat insets dispatch to avoid over-propagation when consuming
        ViewGroupCompat.installCompatInsetsDispatch(binding.root)

        // Apply insets to the root container. Return CONSUMED to prevent stacking/over-padding.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val activityprefs = getSharedPreferences("firstopenctivity", MODE_PRIVATE)
        val firstopenofacticity = activityprefs.getBoolean("first", true)
        if (firstopenofacticity) {
            username()
            activityprefs.edit().putBoolean("first", false).apply()
        }

        // Load saved username at launch
        val savedName = getSharedPreferences(PREFS_USERNAME, MODE_PRIVATE)
            .getString(KEY_USER_NAME, null)
        if (!savedName.isNullOrBlank()) {
            binding.welcome.text = "Welcome,\n $savedName"
        }

        // Edit profile -> set username
        binding.editProfile.setOnClickListener { username() }

        // Theme toggle (let AppCompat recreate Activity)
        val settingsPrefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE)
        var isDark = settingsPrefs.getBoolean(KEY_DARK_MODE, false)
        binding.theme.setOnClickListener {
            val newIsDark = !isDark
            val mode = if (newIsDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
            settingsPrefs.edit().putBoolean(KEY_DARK_MODE, newIsDark).apply()
            isDark = newIsDark
            // No manual recreate; AppCompat handles it
        }

        // Delete account


        // Erase all local data (Room) behind a PIN
        db = Expense_List_Database(this)
        binding.eraseAllData.setOnClickListener {
            checkPin { isCorrect ->
                if (isCorrect) {
                    db.clearAllTables()
                    Snackbar.make(binding.root, "All local data erased", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        binding.deleteContainer.setOnClickListener {
            checkPin { isCorrect ->
                if (isCorrect) {
                    db.clearAllTables()
                    delete_account()
                }
            }

        }

        // Image persistence
        val imagePrefs = getSharedPreferences("imagepref", MODE_PRIVATE)
        val savedImageUri = imagePrefs.getString("image", null)

        // Restore saved image on the correct ImageView
        savedImageUri?.let {
            try {
                val uri = Uri.parse(it)
                contentResolver.openInputStream(uri)?.close() // test access
                binding.profileImageView.setImageURI(uri) // FIX: use profileImageView
            } catch (e: Exception) {
                Log.e("Profile_Page", "Failed to load saved image: ${e.message}")
                imagePrefs.edit().remove("image").apply()
            }
        }

        // Image picker
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    // Persist permission for future access
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (se: SecurityException) {
                    // Some providers may not support persistable grants; ignore if thrown
                    Log.w("Profile_Page", "Persistable grant failed: ${se.message}")
                }

                // Show on the actual profile ImageView
                binding.profileImageView.setImageURI(it)

                // Save the image URI string
                imagePrefs.edit { putString("image", it.toString()) }
            }
        }

        binding.addimagebtn.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun loading_dialog(message: String, time: Long) {
        val dialog = Dialog(this)
        val dialogBinding = LoadingDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700, 800)
        dialogBinding.textonloading.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed(
            { if (dialog.isShowing) dialog.dismiss() },
            time
        )
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
            val prefs = getSharedPreferences(KEY_USER_PIN_FILE, MODE_PRIVATE)
            val stored = prefs.getString(KEY_USER_PIN, null)

            if (pin.isEmpty() || pin.trim() != stored?.trim()) {
                failDialog("Failed.json", "Pin Not Matched", 4000)
            } else {
                onResult(true)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun failDialog(animation: String, message: String, time: Int) {
        val dialog = Dialog(this)
        val dialogBinding = FailureDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.failureDialogLottie.setAnimation(animation)
        dialogBinding.failureDialogLottie.playAnimation()
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(700, 800)
        dialogBinding.textonfailure.text = message
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed(
            { if (dialog.isShowing) dialog.dismiss() },
            time.toLong()
        )
    }

    private fun success_dialog() {
        val dialog = Dialog(this)
        val dialogBinding = SuccessDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(1000, 1000)
        dialogBinding.sucessDialogLottie.setAnimation("DeleteBin.json")
        dialogBinding.sucessDialogLottie.playAnimation()
        dialogBinding.textonsucess.text = "Account Deleted \n Thank You For Using The App"
        dialog.show()
        dialog.setOnDismissListener {
            finish()
            finishAffinity()
        }
    }

    private fun dialogDismiss(dialog: Dialog) {
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, 4000)
    }

    private fun username() {
        val dialog = Dialog(this)
        val dialogBinding = UsernameDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_background))
        dialog.window?.setLayout(900, 1000)
        dialog.show()

        dialogBinding.confirmButton.setOnClickListener {
            val name = dialogBinding.pinEdditext.text.toString().trim()
            if (name.isEmpty()) {
                Snackbar.make(binding.root, "Name cannot be empty", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences(PREFS_USERNAME, MODE_PRIVATE)
            prefs.edit().putString(KEY_USER_NAME, name).apply()

            binding.welcome.text = "Welcome,\n $name"

            dialog.dismiss()
            Snackbar.make(binding.root, "Username Confirmed", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun delete_account() {
        val prefs=getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putBoolean("firstopen", true).apply()
        val activityprefs = getSharedPreferences("firstopenctivity", MODE_PRIVATE)
            .edit().putBoolean("first",true).apply()

        val nameprefs=getSharedPreferences(PREFS_USERNAME,MODE_PRIVATE)
            .edit().putString(KEY_USER_NAME,"").apply()
        val profileimageprefs=getSharedPreferences("imagepref",MODE_PRIVATE)
            .edit().putString("image",null).apply()
        val boolpref=getSharedPreferences("app_prefs",MODE_PRIVATE).getBoolean("firstopen",false)

        if(boolpref){

            success_dialog()
        }

        else{
            failDialog("Failed.json","Something Went Wrong",3000)
        }

    }
}
