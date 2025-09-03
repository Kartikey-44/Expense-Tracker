package ind.shop.expense_tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import ind.shop.expense_tracker.Profile_Page.Companion.PREFS_USERNAME
import ind.shop.expense_tracker.databinding.ActivityBottomMenuBinding
import ind.shop.expense_tracker.databinding.ActivityContactUsBinding
import kotlinx.serialization.descriptors.PrimitiveKind

class Contact_Us : BaseActivity() {
    lateinit var binding: ActivityContactUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name=getSharedPreferences(PREFS_USERNAME,MODE_PRIVATE).getString("user_name",null)

       if(name.isNullOrBlank()){
           binding.contactMessage.text="Welome \n We Are 24*7 Available To Help You"
       }
        else{
           binding.contactMessage.text="Welome $name\n We Are 24*7 Available To Help You"
       }

        val number=9999999
        binding.callUs.setOnClickListener {
            val intent= Intent(Intent.ACTION_DIAL)
            intent.data= Uri.parse("tel: $number")
            startActivity(intent)
        }
        binding.discordUs.setOnClickListener {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://discord.com/channels/@me")
            startActivity(intent)
        }

        binding.mailUs.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://myaccount.google.com/?utm_source=sign_in_no_continue&pli=1")

            startActivity(intent)
        }

        binding.whatsappUs.setOnClickListener {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.data= Uri.parse("https://wa.me/tel:$number?text=${Uri.encode("Hello,I Need Help")}")
            intent.setPackage("com.whatsapp")
            try {
                startActivity(intent)
            }
            catch (e: Exception){
                Snackbar.make(binding.root,"WhataApp Not Installed", Snackbar.LENGTH_LONG).show()
            }
        }

    }
}