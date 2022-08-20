package com.dsoki.crispoclient.ui.cart

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsoki.crispoclient.Adapter.MyCartAdapter
import com.dsoki.crispoclient.CallBack.IMyButtonCallBack
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Common.MySwipeHelper
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.LocalCartDateSource
import com.dsoki.crispoclient.EventBus.CountCartEvent
import com.dsoki.crispoclient.EventBus.HideFABCart
import com.dsoki.crispoclient.EventBus.updateItemsInCart
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.Order
import com.google.android.gms.location.*
import com.google.android.play.core.internal.e
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.layout_place_order.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.*


class CartFragment  : Fragment() {

    private lateinit var cartViewModel :CartViewModel
    private var compositeDisposable : CompositeDisposable = CompositeDisposable()
    private var recyclerViewState:Parcelable?=null
    private var cartDataSource: CartDataSource?=null

    private lateinit var btn_place_order:Button
    var txt_empty_cart :TextView?=null
    var txt_total_prices :TextView?=null
    var group_place_holder :CardView?=null
    var recycler_cart :RecyclerView?=null
    var adapter:MyCartAdapter?=null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallBack: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation:Location

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallBack,
            Looper.myLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        EventBus.getDefault().postSticky(HideFABCart(true))
        cartViewModel = ViewModelProviders.of(this).get(CartViewModel::class.java)
        //after create cartViewModel ,init dataSource
        cartViewModel.initCartDataSource(requireContext())
        val root = LayoutInflater.from(context).inflate(R.layout.fragment_cart,container,false)
        initViews(root)
        initLocation()
        cartViewModel.getMutableLiveDataCartItems().observe(viewLifecycleOwner, Observer {
            if (it==null || it.isEmpty())
            {
                recycler_cart!!.visibility =View.GONE
                group_place_holder!!.visibility =View.GONE
                txt_empty_cart!!.visibility =View.GONE
            }
            else{
                recycler_cart!!.visibility =View.VISIBLE
                group_place_holder!!.visibility =View.VISIBLE
                txt_empty_cart!!.visibility =View.GONE

                 adapter =MyCartAdapter(requireContext(),it)
                recycler_cart!!.adapter =adapter
                }
            })
        return root
    }

    private fun initLocation() {
        buildLocationRequest()
        buildLocationCallBack()
        fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireActivity()!!.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity()!!.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallBack
            ,Looper.getMainLooper())
    }

    private fun buildLocationCallBack() {
        locationCallBack =object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                currentLocation =p0!!.lastLocation
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(3000)
        locationRequest.setSmallestDisplacement(10f)
    }

    @SuppressLint("MissingPermission")
    private fun initViews(root:View) {
        setHasOptionsMenu(true)//if you don't add it , menu would never be inflate .
        cartDataSource=LocalCartDateSource(CartDatabase.getInstance(requireContext()).cartDao())
        recycler_cart =root.findViewById(R.id.recycler_cart)
        recycler_cart!!.setHasFixedSize(true)
        val layoutManger =LinearLayoutManager(context)
        recycler_cart!!.layoutManager =layoutManger
        recycler_cart!!.addItemDecoration(DividerItemDecoration(context,layoutManger.orientation))

        val swipe = object :  MySwipeHelper(requireContext(),recycler_cart!!,200)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Remove",
                    30,
                    0,
                    Color.parseColor("#FF3C30"),
                    object : IMyButtonCallBack{
                    override fun onClick(pos: Int) {
                        //Toast.makeText(context,"Delete Item",Toast.LENGTH_LONG).show()
                        val deleteItem =adapter!!.getItemPosition(pos)
                        cartDataSource!!.deleteCart(deleteItem!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : SingleObserver<Int>{
                                override fun onSubscribe(d: Disposable) {
                                }
                                override fun onSuccess(t: Int) {
                                    adapter!!.notifyItemRemoved(pos)
                                    sumCart()
                                    EventBus.getDefault().postSticky(CountCartEvent(true))
                                    Toast.makeText(context,"Delete Item Success",Toast.LENGTH_LONG).show()
                                }
                                override fun onError(e: Throwable) {
                                }

                            } )
                        }
                    }))
                }}

        txt_empty_cart =  root.findViewById(R.id.txt_empty_cart) as TextView
        txt_total_prices =root.findViewById(R.id.txt_total_price) as TextView
        group_place_holder =root.findViewById(R.id.group_palce_holder) as CardView
        btn_place_order =root.findViewById(R.id.btn_place_order) as Button

        //Event
        btn_place_order.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("One more step!")

            val view =LayoutInflater.from(context).inflate(R.layout.layout_place_order,null)
            val edt_address =view.findViewById<View>(R.id.edtAddress) as EditText
            val edt_comment =view.findViewById<View>(R.id.edtComment) as EditText
            val txt_address =view.findViewById<View>(R.id.txt_address_detail) as TextView
            val rdi_home =view.findViewById<View>(R.id.rdi_home_address) as RadioButton
            val rdi_other_address =view.findViewById<View>(R.id.rdi_other_address) as RadioButton
            val rdi_ship_to_this_address =view.findViewById<View>(R.id.rdi_ship_this_address) as RadioButton
            val rdi_cod =view.findViewById<View>(R.id.rdi_cod) as RadioButton
            val rdi_braintree =view.findViewById<View>(R.id.rdi_braintree) as RadioButton

            //Data
            edt_address.setText(Common.currentUser!!.address!!)
            rdi_home.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                {
                    edt_address.setText(Common.currentUser!!.address!!)
                    txt_address.visibility=View.GONE
                }
            }
            rdi_other_address.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                {
                    edt_address.setText("")
                    edt_address.setHint("Enter address")
                    txt_address.visibility=View.GONE
                }
            }
            rdi_ship_to_this_address.setOnCheckedChangeListener { compoundButton, b ->
                if (b)
                {
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity()!!.applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (ActivityCompat.checkSelfPermission(
                                requireActivity()!!.applicationContext,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                           // return
                        }
                    }
                    fusedLocationProviderClient!!.lastLocation!!.addOnFailureListener{ e ->
                    txt_address.visibility=View.GONE
                    Toast.makeText(requireContext(),""+e.message,Toast.LENGTH_LONG).show()}
                        .addOnCompleteListener{
                            task ->
                            val coordinates = StringBuilder().append(task.result!!.latitude)
                                .append("/")
                                .append(task.result!!.longitude).toString()

                            val singleAddress = Single.just(getAddressFromLatLng(task.result!!.latitude,task.result!!.longitude))
                            val disposable =singleAddress.subscribeWith(object:DisposableSingleObserver<String>(){
                                override fun onSuccess(t: String) {
                                    edt_address.setText(coordinates)
                                    txt_address.visibility=View.VISIBLE
                                    txt_address.setText(t)
                                }
                                override fun onError(e: Throwable) {
                                    edt_address.setText(coordinates)
                                    txt_address.visibility=View.VISIBLE
                                    txt_address.setText(e.message!!)
                                }
                            })
                        }
                }
            }


            builder.setView(view)
            builder.setNegativeButton("NO",{dialogInterface, _ -> dialogInterface.dismiss()})
           .setPositiveButton("YES",{dialogInterface, _ ->
               if(rdi_cod.isChecked)
                   paymentCOD(edt_address.text.toString(),edt_comment.text.toString())
                })
            val dialog =builder.create()
            dialog.show()
        }
        }

    private fun paymentCOD(address: String, comment: String) {
        compositeDisposable.add(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cartItemList ->
                //when we have all cartItem , we will get total price
                cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<Double>{
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onSuccess(totalPrice: Double) {
                            val finalPrice =totalPrice
                            val order =Order()
                            order.userId =Common.currentUser!!.uid
                            order.userName =Common.currentUser!!.name
                            order.userPhone =Common.currentUser!!.phone
                            order.shippingAddress =address
                            order.comment =comment
                            order.lat =currentLocation.latitude
                            order.lng =currentLocation.longitude
                            order.cartItemList =cartItemList
                            order.finalPayment =finalPrice
                            order.totalPayment =totalPrice
                            order.discount =0
                            order.isCod =true
                            order.transactionId ="Cash on Delivery"

                            writeOrderToFirebase(order)
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(requireContext(),
                                ""+e.message,
                                Toast.LENGTH_LONG).show()
                        }
                    })

            } , { throwable ->
                Toast.makeText(requireContext(),
                    ""+throwable.message,
                    Toast.LENGTH_LONG).show()
            }))

    }

    private fun writeOrderToFirebase(order: Order) {
    FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
        .child(Common.createOrderNumber())
        .setValue(order)
        .addOnFailureListener{e ->Toast.makeText(requireContext(),""+e.message,Toast.LENGTH_LONG).show()}
        .addOnCompleteListener{ task ->
            if (task.isSuccessful)
            {
                cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object :SingleObserver<Int>{
                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onSuccess(t: Int) {
                            Toast.makeText(requireContext(),"Order Placed successfully",Toast.LENGTH_LONG).show()
                        }

                        override fun onError(e: Throwable) {
                            Toast.makeText(requireContext(),""+e.message,Toast.LENGTH_LONG).show()
                        }

                    })
            }
        }
    }

    private fun getAddressFromLatLng(latitude: Double, longitude: Double): String {
        val geoCoder =Geocoder(requireContext(), Locale.getDefault())
        var result:String?=null
        try {
            val addressList=geoCoder.getFromLocation(latitude,longitude,1)
            if (addressList !=null && addressList.size>0)
            {
                val address =addressList[0]
                val sb =StringBuilder(address.getAddressLine(0))
                result =sb.toString()
            }
            else
                result ="address not found"
            return result
        }catch (e:IOException){return e.message!!}
    }

    private fun sumCart() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(t: Double) {
                    txt_total_prices!!.text =StringBuilder("Total : $ ").append(t)
                }
                override fun onError(e: Throwable) {
                    if(!e.message!!.contains("Query returned empty"))
                   Toast.makeText(context,""+e.message!!,Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
              EventBus.getDefault().register(this)
    }

    override fun onStop() {
        cartViewModel.onStop()
        compositeDisposable.clear()
        EventBus.getDefault().postSticky(HideFABCart(false))
        if (!EventBus.getDefault().isRegistered(this))
             EventBus.getDefault().unregister(this)
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        super.onStop()
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun UpdateItemInCart(event: updateItemsInCart){
        recyclerViewState =recycler_cart!!.layoutManager!!.onSaveInstanceState()
        cartDataSource!!.updateCart(event.cartItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object:SingleObserver<Int>{
                override fun onSubscribe(d: Disposable){
                }
                override fun onSuccess(t: Int) {
                    calculateTotalPrice()
                    recycler_cart!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
                }
                override fun onError(e: Throwable) {
                    Toast.makeText(context,"[UPDATE CART]"+e.message,Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun calculateTotalPrice() {
        cartDataSource!!.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object:SingleObserver<Double>{
                override fun onSubscribe(d: Disposable) {
                }
                override fun onSuccess(price: Double) {
                    txt_total_prices!!.text =StringBuilder("Total: $").append(Common.formatPrice(price))
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                    Toast.makeText(context,"[SUM CART]"+e.message,Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu!!.findItem(R.id.action_settings).setVisible(false)//hide menu setting when in cart
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item!!.itemId==R.id.action_clear_cart)
        {
            cartDataSource!!.cleanCart(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int>{
                    override fun onSubscribe(d: Disposable) {
                    }
                    override fun onSuccess(t: Int) {
                        Toast.makeText(context,"Clear Cart Success",Toast.LENGTH_LONG).show()
                        EventBus.getDefault().postSticky(CountCartEvent(true))
                    }
                    override fun onError(e: Throwable) {
                        Toast.makeText(context,""+e.message,Toast.LENGTH_LONG).show()
                    }
                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}


