package com.dsoki.crispoclient

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.LocalCartDateSource
import com.dsoki.crispoclient.EventBus.*
import com.dsoki.crispoclient.model.CategoryModel
import com.dsoki.crispoclient.model.FoodModel
import com.dsoki.crispoclient.ui.cart.CartFragment
import com.dsoki.crispoclient.ui.home.HomeFragment
import com.dsoki.crispoclient.ui.menu.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var cartDataSource: CartDataSource
    private lateinit var navController:NavController

    private var drawer :DrawerLayout?=null
    private var dialog:android.app.AlertDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val chipNavigationBar : ChipNavigationBar = findViewById(R.id.bottom_nav)

        chipNavigationBar.setOnItemSelectedListener() { id ->
             when (id) {
                 R.id.nav_home -> {
                     navController.navigate(R.id.nav_home)
                 }
                 R.id.nav_menu -> {
                     navController.navigate(R.id.nav_menu)
                 }
                 R.id.nav_cart -> {
                     navController.navigate(R.id.nav_cart)
                 }
             }
         }

             dialog =SpotsDialog.Builder().setContext(this).setCancelable(false).build()
             cartDataSource =LocalCartDateSource(CartDatabase.getInstance(this).cartDao())

             val toolbar: Toolbar = findViewById(R.id.toolbar)
             setSupportActionBar(toolbar)

             val fab: FloatingActionButton = findViewById(R.id.fab)
             fab.setOnClickListener { view ->
                 navController.navigate(R.id.nav_cart)
             }

             drawer= findViewById(R.id.drawer_layout)
             val navView: NavigationView = findViewById(R.id.nav_view)
             navController = findNavController(R.id.nav_host_fragment)
             // Passing each menu ID as a set of Ids because each
             // menu should be considered as top level destinations.
             appBarConfiguration = AppBarConfiguration(
                 setOf(
                     R.id.nav_home, R.id.nav_menu, R.id.nav_foodList,
                     R.id.nav_commentList, R.id.nav_cart,//R.id.nav_sign_out
                 ), drawer
             )

             setupActionBarWithNavController(navController, appBarConfiguration)
             navView.setupWithNavController(navController)

             var headerView =navView.getHeaderView(0)
             var txt_user =headerView.findViewById<TextView>(R.id.txt_user)
             Common.setSpanString("Hey, ", Common.currentUser!!.name, txt_user)

             navView.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
                 override fun onNavigationItemSelected(item: MenuItem): Boolean {
                     item.isChecked = true
                     drawer!!.closeDrawers()
                     if (item.itemId == R.id.nav_sign_out) {
                         signOut()
                     } else if (item.itemId == R.id.nav_home) {
                         navController.navigate(R.id.nav_home)
                     } else if (item.itemId == R.id.nav_cart) {
                         navController.navigate(R.id.nav_cart)
                     } else if (item.itemId == R.id.nav_menu) {
                         navController.navigate(R.id.nav_menu)
                     } else if (item.itemId == R.id.nav_commentList) {
                         navController.navigate(R.id.nav_commentList)
                     } else if (item.itemId == R.id.nav_foodList) {
                         navController.navigate(R.id.nav_foodList)
                     }
                     return true
                 }
             })
             countCartItem()
         }

    private fun signOut() {
        val builder =androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Sign Out").setMessage("Do you really want to Exits ?")
               .setNegativeButton("CANCEL", { dialogInterface, _ -> dialogInterface.dismiss() })
               .setPositiveButton("OK"){ dialogInterface, _ ->
                   Common.foodSelected =null
                   Common.categorySelected =null
                   Common.currentUser =null
                   FirebaseAuth.getInstance().signOut()

                   val intent =Intent(this@HomeActivity, MainActivity::class.java)
                   intent.flags =Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                   startActivity(intent)
                   finish()
               }
        val dialog =builder.create()
        dialog.show()
        }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onResume() {
        super.onResume()
        countCartItem()
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick)
    {
        if (event.isSuccess){
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_foodList)
               // Toast.makeText(this,"Click to"+event.Category.name,Toast.LENGTH_LONG).show()
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodListSelected(event: FoodClick)
    {
        if (event.isSuccess){
            findNavController(R.id.nav_host_fragment).navigate(R.id.nav_food_detail)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFABEvent(event: HideFABCart)
    {
        if (event.isHide){
            fab.hide()
        }
        else
            fab.show()
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CountCartEvent)
    {
        if (event.isSuccess){
         countCartItem()
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPopularFoodItemClick(event: PopularFoodItemClick)
    {
        if(event.popularCategoryModel !=null)
        {
            dialog!!.show()
            FirebaseDatabase.getInstance().getReference("Category").child(event.popularCategoryModel!!.menu_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "" + p0.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)
                            Common.categorySelected!!.menu_id = p0.key

                            //Load Food
                            FirebaseDatabase.getInstance().getReference("Category")
                                .child(event.popularCategoryModel!!.menu_id!!).child("foods")
                                .orderByChild("id")
                                .equalTo(event.popularCategoryModel!!.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "item is not exist",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            for (foodSnapShot in p0.children) {
                                                Common.foodSelected =
                                                    foodSnapShot.getValue(FoodModel::class.java)
                                                Common.foodSelected!!.key = foodSnapShot.key
                                            }
                                            navController.navigate(R.id.nav_food_detail)
                                        } else {
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "item is not exist",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        dialog!!.dismiss()
                                    }
                                })

                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "item is not exist",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onBestDealsFoodItem(event: BestDealsItemClick)
    {
        if(event.model != null)
        {
            dialog!!.show()
            FirebaseDatabase.getInstance().getReference("Category").child(event.model!!.menu_id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        dialog!!.dismiss()
                        Toast.makeText(this@HomeActivity, "" + p0.message, Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            Common.categorySelected = p0.getValue(CategoryModel::class.java)
                            Common.categorySelected!!.menu_id = p0.key
                            //Load Food
                            FirebaseDatabase.getInstance().getReference("Category")
                                .child(event.model!!.menu_id!!).child("foods")
                                .orderByChild("id")
                                .equalTo(event.model!!.food_id)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {
                                        dialog!!.dismiss()
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "item is not exist",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            for (foodSnapShot in p0.children) {
                                                Common.foodSelected =
                                                    foodSnapShot.getValue(FoodModel::class.java)
                                                Common.foodSelected!!.key = foodSnapShot.key
                                            }
                                            navController.navigate(R.id.nav_food_detail)
                                        } else {
                                            Toast.makeText(
                                                this@HomeActivity,
                                                "item is not exist",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        dialog!!.dismiss()
                                    }
                                })

                        } else {
                            dialog!!.dismiss()
                            Toast.makeText(
                                this@HomeActivity,
                                "item is not exist",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
        }
    private fun countCartItem()
    {
       cartDataSource.countItemInCart(Common.currentUser!!.uid!!)
           .subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread())
           .subscribe(object : SingleObserver<Int> {
               override fun onSubscribe(d: Disposable) {
               }

               override fun onSuccess(t: Int) {
                   fab.count = t
               }

               override fun onError(e: Throwable) {
                   if (!e.message!!.contains("Query returned empty"))
                       Toast.makeText(
                           this@HomeActivity,
                           "[COUNT CART]" + e.message,
                           Toast.LENGTH_LONG
                       ).show()
                   else
                       fab.count = 0
               }
           })
      }
}