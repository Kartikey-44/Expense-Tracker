package ind.shop.expense_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ind.shop.expense_tracker.databinding.ActivityBottomMenuBinding

open class BaseActivity : AppCompatActivity() {

    // Nullable; only set when the current layout actually contains the bottom menu
    protected var bottomMenuBinding: ActivityBottomMenuBinding? = null

    override fun onStart() {
        super.onStart()

        // Try to find the bottom menu root in the current content view.
        // bottom_nav_card must be the root of activity_bottom_menu.xml (or the include's id).
        val bottomRoot: View? = findViewById(R.id.bottom_nav_card)
        if (bottomRoot != null) {
            bottomMenuBinding = ActivityBottomMenuBinding.bind(bottomRoot)

            bottomMenuBinding?.addtask?.setOnClickListener {
                val intent = Intent(this, Add_Edit_Page::class.java)
                intent.putExtra(Add_Edit_Page.Action_Type, "add")
                startActivity(intent)
            }

            bottomMenuBinding?.viewTransaction?.setOnClickListener {
                startActivity(Intent(this, Category_Page::class.java))
            }

            bottomMenuBinding?.profile?.setOnClickListener {
                startActivity(Intent(this, Profile_Page::class.java))
            }

            bottomMenuBinding?.contactUs?.setOnClickListener {
                startActivity(Intent(this, Contact_Us::class.java))
            }

            bottomMenuBinding?.dashboard?.setOnClickListener {
                // Avoid launching another copy of the same screen if already on it, if desired
                startActivity(Intent(this, User_Dashboard::class.java))
            }
        } else {
            // This Activity doesn't have the bottom menu in its layout.
            bottomMenuBinding = null
        }
    }

    override fun onStop() {
        super.onStop()
        // Clear reference to avoid leaking views
        bottomMenuBinding = null
    }
}
