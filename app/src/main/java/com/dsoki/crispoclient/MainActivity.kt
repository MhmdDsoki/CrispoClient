package com.dsoki.crispoclient
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Remote.ICloudFunctions
import com.dsoki.crispoclient.model.UserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.disposables.CompositeDisposable
import java.security.Permission
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var dialog:android.app.AlertDialog
    private val compositeDisposal=CompositeDisposable()
 // private lateinit var cloudFunctions: ICloudFunctions
    private lateinit var userRef:DatabaseReference
    private  var providers:List<AuthUI.IdpConfig>?=null
    //Object declarations
    //If you need a singleton - a class that only has got one instance -
    // you can declare the class in the usual way, but use the object keyword instead of class:
    // object CarFactory {
        //val cars = mutableListOf<Car>()
        //  fun makeCar(horsepowers: Int): Car {
        //    val car = Car(horsepowers)
        //      cars.add(car)
        //            return car
        //    }}
   //Companion objects
    //If you need a function or a property to be tied to a class rather than to instances
    // of it (similar to @staticmethod in Python), you can declare it inside a companion object:
    //class Car(val horsepowers: Int) {
    //    companion object Factory {
    //        val cars = mutableListOf<Car>()
    //        fun makeCar(horsepowers: Int): Car {
    //            val car = Car(horsepowers)
    //            cars.add(car)
    //            return car
    //        }   }}
    //These are object declarations inside a class.
    // These companion objects are initialized when the containing class is resolved,
    // similar to static methods and variables in java world.
    //e.g.
    companion object{
        private val APP_REQUEST_CODE=911
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener (listener)
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(listener)
        compositeDisposal.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        providers = listOf(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef=FirebaseDatabase.getInstance().getReference(com.dsoki.crispoclient.Common.Common.USER_REFEREENCE)
        firebaseAuth=FirebaseAuth.getInstance()
        dialog=SpotsDialog.Builder().setContext(this).setCancelable(false).build()
//      cloudFunctions=RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener=FirebaseAuth.AuthStateListener { firebaseAuth ->
        Dexter.withActivity(this@MainActivity)
        .withPermission(android.Manifest.permission
        .ACCESS_FINE_LOCATION)
        .withListener(object: PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    val user =firebaseAuth.currentUser
                    if (user!=null)
                    {
                        checkUserFromFirebase(user)
                    }
                    else{
                        phoneLogin()
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@MainActivity,"must accept this permissionto use app",Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                }

            }).check()
        }
    }

    private fun checkUserFromFirebase(user:FirebaseUser) {
        dialog.show()
        userRef.child(user.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@MainActivity,""+p0.message,Toast.LENGTH_LONG)
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        val userModel=snapshot.getValue(UserModel::class.java)
                        goToHomeActivity(userModel)
                    }
                    else
                    {
                        showRegisterDialog(user)
                    }
                    dialog.dismiss()
                }
            })
    }

    private fun showRegisterDialog(user:FirebaseUser) {
        val builder =androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Register")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(this@MainActivity).inflate(R.layout.register_layout,null)

        val edname =itemView.findViewById<EditText>(R.id.editName)
        val edAddress =itemView.findViewById<EditText>(R.id.edAddress)
        val edPhone =itemView.findViewById<EditText>(R.id.edPhone)
        //set
        edPhone.setText(user.phoneNumber.toString())
        builder.setView(itemView)
        builder.setNegativeButton("Cancel"){dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("Register"){dialogInterface, i ->

            if (TextUtils.isDigitsOnly(edname.text.toString())){
                Toast.makeText(this@MainActivity,"Enter your name",Toast.LENGTH_LONG)
                return@setPositiveButton
            }
            else if (TextUtils.isDigitsOnly(edAddress.text.toString())){
                Toast.makeText(this@MainActivity,"Enter your Address",Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }

            val userModel =UserModel()
            userModel.uid =user.uid
            userModel.name=edname.text.toString()
            userModel.address=edAddress.text.toString()
            userModel.phone=edPhone.text.toString()

            userRef.child(user.uid).setValue(userModel).addOnCompleteListener{task ->
                if (task.isSuccessful)
                {
                    dialogInterface.dismiss()
                    Toast.makeText(this@MainActivity,"Register success",Toast.LENGTH_LONG).show()
                    goToHomeActivity(userModel)
                }
            }
        }
        val dialog=builder.create()
        dialog.show()
    }

    private fun goToHomeActivity(userModel: UserModel?) {
        Common.currentUser=userModel!!
        startActivity(Intent(this@MainActivity,HomeActivity::class.java))
        finish()
    }

    private fun getCustomToken(accesstoken: Any) {
    }

    private fun phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder().setAvailableProviders(providers!!).build(),APP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== APP_REQUEST_CODE)
        {
            val responde =IdpResponse.fromResultIntent(data)
            if (resultCode== Activity.RESULT_OK)
            {
                val user =FirebaseAuth.getInstance().currentUser
            }
            else
            {
                Toast.makeText(this,"failed to sign in",Toast.LENGTH_LONG).show()
            }
        }
    }

}
