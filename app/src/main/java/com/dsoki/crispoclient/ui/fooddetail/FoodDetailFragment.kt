package com.dsoki.crispoclient.ui.fooddetail

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andremion.counterfab.CounterFab
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.CartItem
import com.dsoki.crispoclient.Database.LocalCartDateSource
import com.dsoki.crispoclient.EventBus.CountCartEvent
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.CommentModel
import com.dsoki.crispoclient.model.FoodModel
import com.dsoki.crispoclient.ui.comment.CommentFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_fooddetail.*
import org.greenrobot.eventbus.EventBus

class FoodDetailFragment : Fragment(), TextWatcher {
    private lateinit var cartDataSource:CartDataSource
    private val compositeDisposable= CompositeDisposable()

    private  lateinit var  foodDetailViewModel: FoodDetailViewModel
    private  lateinit var addonBottomSheetDialog: BottomSheetDialog

    private var imgFood: ImageView ? =null
    private var btnCart: CounterFab ? =null
    private var btnRating: FloatingActionButton ? =null
    private var food_name: TextView?=null
    private var food_Description: TextView?=null
    private var food_Price: TextView?=null
    private var number_button: ElegantNumberButton?=null
    private var ratingBar: RatingBar?=null
    private var btnShowComment: Button?=null
    private var waitingDialog:AlertDialog?=null
    private var rdiGroupSize:RadioGroup?=null

    private var img_add_on:ImageView ?=null
    private lateinit var chip_group_user_selected_addOn : ChipGroup
    //addon layout
    private var chip_group_addon : ChipGroup?=null
    private var search_edt_addon : EditText?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodDetailViewModel =
            ViewModelProviders.of(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_fooddetail, container, false)
        initViews(root)
        foodDetailViewModel.getMutablieLifeDataDetailFood().observe(viewLifecycleOwner, Observer {
           displayInfo(it)
        })
        foodDetailViewModel.getMutablieLifeDatacomment().observe(viewLifecycleOwner,Observer{
            subbmitRatingToFireBase(it)
        })
        return root
    }
    //SEARCH
    override fun afterTextChanged(p0: Editable?) {
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
    override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
        chip_group_addon!!.clearCheck()
        chip_group_addon!!.removeAllViews()
        for (AddonModel in Common.foodSelected!!.addon)
        {
            if (AddonModel.name!!.toLowerCase().contains(charSequence.toString().toLowerCase()))
            {
                val chip=layoutInflater.inflate(R.layout.chip_layout,null,false) as Chip
                chip.text=StringBuilder(AddonModel.name!!).append("(+$").append(AddonModel.price).append(")").toString()
                chip.setOnCheckedChangeListener{compoundButton, b ->
                    if (b)
                    {
                        if (Common.foodSelected!!.userSelectedAddon ==null)
                             Common.foodSelected!!.userSelectedAddon=ArrayList()
                        Common.foodSelected!!.userSelectedAddon!!.add(AddonModel)
                    }
                }
                chip_group_addon!!.addView(chip)
            }
        }
    }

    private fun displayInfo(it: FoodModel?) {
        Glide.with(requireContext()).load(it!!.image).into(img_food!!)
        food_name!!.text=StringBuilder(it.name!!)
        food_Description!!.text=StringBuilder(it.description!!)
        food_Price!!.text=StringBuilder(it.price!!.toString())
        ratingBar!!.rating =it.ratingValue!!.toFloat() / it.ratingCount!!
        
        //set Size
        for(sizeModel in it.size)
        {
            val radioButton=RadioButton(context)
            radioButton.setOnCheckedChangeListener{ compoundButton, b ->
                if (b)
                    Common.foodSelected!!.userSelectedSize =sizeModel
                calculateTotalPrice()
            }
            val params =LinearLayout.LayoutParams(0 , LinearLayout.LayoutParams.MATCH_PARENT , 1.0f)
            radioButton.layoutParams=params
            radioButton.text = sizeModel.name
            radioButton.tag  = sizeModel.price
            rdiGroupSize!!.addView(radioButton)
            }
         //default first radio button selected
        if (rdiGroupSize!!.childCount > 0)
        {
            val radioButton = rdiGroupSize!!.getChildAt(0) as RadioButton
            radioButton.isChecked =true
        }
    }

    private fun calculateTotalPrice() {
      var totalPrice =Common.foodSelected!!.price!!.toDouble()
      var displayPrice = 0.0
        //addon
            if(Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0)
            {
                for (AddonModel in Common.foodSelected!!.userSelectedAddon!!)
                {
                    totalPrice += AddonModel.price!!.toDouble()
                }
            }
        //size
        totalPrice += Common.foodSelected!!.userSelectedSize!!.price!!.toDouble()
        displayPrice = totalPrice!! * number_button!!.number.toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0
        food_Price!!.text =StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    private fun subbmitRatingToFireBase(commentModel:CommentModel?) {
        waitingDialog!!.show()
        FirebaseDatabase.
        getInstance().
        getReference(Common.COMMENT_REF!!).
        child(Common.foodSelected!!.id!!)
        .push().setValue(commentModel).addOnCompleteListener{ task ->
                if (task.isSuccessful)
                {
                    addRatingToFood(commentModel!!.ratingValue.toDouble())
                }
                    waitingDialog!!.dismiss()
            }
    }
    private fun addRatingToFood(ratingValue:Double)
    {
        FirebaseDatabase.getInstance().getReference(Common.CATEGORIES_REF)
                            .child(Common.categorySelected!!.menu_id!!)//select menu in category
                            .child("foods") //select foods array
                            .child(Common.foodSelected!!.key!!) //select key
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                    waitingDialog!!.dismiss()
                                    Toast.makeText(context,""+p0.message,Toast.LENGTH_LONG).show()
                                                }
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists())
                                    {
                                        val foodModel =dataSnapshot.getValue(FoodModel::class.java)
                                        foodModel!!.key =Common.foodSelected!!.key
                                        //Apply rating
                                        val sumRating =foodModel.ratingValue!!.toDouble() + ratingValue
                                        val ratingCount =foodModel.ratingCount!!+1
                                        //val result =sumRating/ratingCount

                                        val updateData =HashMap<String,Any>()
                                        updateData["ratingValue"]=sumRating
                                        updateData["countValue"] =ratingCount

                                        //update data in variable
                                        foodModel.ratingCount =ratingCount
                                        foodModel.ratingValue=sumRating
                                        dataSnapshot.ref.updateChildren(updateData).addOnCompleteListener{task ->
                                            waitingDialog!!.dismiss()
                                            if(task.isSuccessful)
                                            {
                                                Common.foodSelected=foodModel
                                                foodDetailViewModel!!.setFoodModel(foodModel)
                                                Toast.makeText(context,"Thank you",Toast.LENGTH_LONG).show()
                                            }
                                        }
                                       }
                                    else {
                                        waitingDialog!!.dismiss()
                                    }
                                }
                            })
                       }

    private fun initViews(root: View?) {
        cartDataSource =LocalCartDateSource(CartDatabase.getInstance(requireContext()).cartDao())
        addonBottomSheetDialog=BottomSheetDialog(requireContext(),R.style.DialogStyle)
        val layout_user_selected_addOn = layoutInflater.inflate(R.layout.layout_addon_display,null)
        chip_group_addon =layout_user_selected_addOn.findViewById(R.id.chip_group_addon) as ChipGroup
        search_edt_addon =layout_user_selected_addOn.findViewById(R.id.search_edt_addon) as EditText
        addonBottomSheetDialog.setContentView(layout_user_selected_addOn)

        addonBottomSheetDialog!!.setOnDismissListener{dialogInterface ->
            displayUserSelectedAddon()
            calculateTotalPrice()
        }
        waitingDialog =SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        btnCart =root!!.findViewById(R.id.btnCrt) as CounterFab
        btnRating =root.findViewById(R.id.btnrating) as FloatingActionButton
        btnShowComment =root.findViewById(R.id.btnShowComment) as Button
        food_name =root.findViewById(R.id.food_name) as TextView
        food_Description =root.findViewById(R.id.food_desc) as TextView
        food_Price =root.findViewById(R.id.fod_price) as TextView
        imgFood =root.findViewById(R.id.img_food) as ImageView
        ratingBar=root.findViewById(R.id.ratingBar) as RatingBar
        number_button=root.findViewById(R.id.nmbr_btn) as ElegantNumberButton
        rdiGroupSize =root.findViewById(R.id.rdi_group_size) as RadioGroup
        chip_group_user_selected_addOn = root.findViewById(R.id.chip_group_user_selected_addOn) as ChipGroup
        img_add_on = root.findViewById(R.id.img_add_addon) as ImageView

        //Event
        img_add_on!!.setOnClickListener{
            displayAllAddon()
            addonBottomSheetDialog.show()
        }
        btnRating!!.setOnClickListener{
            showDialogRating()
        }
        btnShowComment!!.setOnClickListener{
            val commentFragment =CommentFragment.getInstance()
                commentFragment.show(requireActivity().supportFragmentManager,"CommentFragment")
        }
        btnCart!!.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid=Common.currentUser!!.uid
            cartItem.userPhone=Common.currentUser!!.phone

            cartItem.foodId=Common.foodSelected!!.id
            cartItem.foodName = Common.foodSelected!!.name
            cartItem.foodImage=Common.foodSelected!!.image
            cartItem.foodPrice=Common.foodSelected!!.price!!.toDouble()

            cartItem.foodQuantity = number_button!!.number.toInt()
            cartItem.foodExtraPrice =Common.calculateExtraPrice(Common.foodSelected!!.userSelectedSize,Common.foodSelected!!.userSelectedAddon)

            if (Common.foodSelected!!.userSelectedAddon != null)
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            else
                cartItem.foodAddon="Default"

            if (Common.foodSelected!!.userSelectedSize!=null)
                cartItem.foodSize =Gson().toJson(Common.foodSelected!!.userSelectedSize)
            else
             cartItem.foodSize ="Default"

            cartDataSource.getItemWithAllOptionsInCart(Common.currentUser!!.uid!!,
                cartItem.foodId!!.toString(),
                cartItem.foodSize!!,
                cartItem.foodAddon!!).
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
            subscribe(object : SingleObserver<CartItem> {
                override fun onSubscribe(d: Disposable) {

                }
                override fun onSuccess(cartItemFromDB: CartItem) {

                    if (cartItemFromDB.equals(cartItem))
                    {
                        //if item already in db just update.
                        cartItemFromDB.foodExtraPrice =cartItem.foodExtraPrice
                        cartItemFromDB.foodAddon =cartItem.foodAddon
                        cartItemFromDB.foodSize =cartItem.foodSize
                        cartItemFromDB.foodQuantity = cartItemFromDB.foodQuantity!! + cartItem.foodQuantity!!

                        cartDataSource.updateCart(cartItemFromDB).
                        subscribeOn(Schedulers.io()).
                        observeOn(AndroidSchedulers.mainThread()).
                        subscribe(object : SingleObserver<Int>{
                            override fun onSuccess(t: Int) {
                                Toast.makeText(context,"Update cart success",Toast.LENGTH_LONG).show()
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            }
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(context,"[UPDATE CART]"+e.message,Toast.LENGTH_LONG).show()
                            }
                         })
                    }
                    else{
                        //if item not available in db ,just insert.
                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Toast.makeText(context,"Add to cart successfully",Toast.LENGTH_LONG).show()
                                //we will send  notify to Home Activity to update counterFab.
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            } , {
                                    t:Throwable ? ->Toast.makeText(context,"[INSERT CART]"+t!!.message,Toast.LENGTH_LONG).show()
                            }))
                    }
                }

                override fun onError(e: Throwable) {
                    if (e.message!!.contains("empty"))
                    {
                        compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                Toast.makeText(context,"Add to cart successfully",Toast.LENGTH_LONG).show()
                                //we will send  notify to Home Activity to update counterFab.
                                EventBus.getDefault().postSticky(CountCartEvent(true))
                            } , {
                                    t:Throwable ? ->Toast.makeText(context,"[INSERT CART]"+t!!.message,Toast.LENGTH_LONG).show()
                            }))
                    }
                    else {
                        Toast.makeText(context,"[CART ERROR]"+e.message,Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun displayAllAddon() {
     if (Common.foodSelected!!.addon.size > 0)
         {
            chip_group_addon!!.clearCheck()
            chip_group_addon!!.removeAllViews()
            search_edt_addon!!.addTextChangedListener(this)

             for (AddonModel in Common.foodSelected!!.addon)
             {
                 val chip =layoutInflater.inflate(R.layout.chip_layout,null,false) as Chip
                     chip.text=StringBuilder(AddonModel.name!!).append("(+$").append(AddonModel.price).append(")").toString()
                     chip.setOnCheckedChangeListener{compoundButton, b ->
                         if (b)
                         {
                             if (Common.foodSelected!!.userSelectedAddon ==null)
                                   Common.foodSelected!!.userSelectedAddon = ArrayList()
                                 Common.foodSelected!!.userSelectedAddon!!.add(AddonModel)

                         }
                     }
                  chip_group_addon!!.addView(chip)
               }
          }
      }

   @SuppressLint("InflateParams")
    private fun displayUserSelectedAddon() {
        if (Common.foodSelected!!.userSelectedAddon !=null && Common.foodSelected!!.userSelectedAddon!!.size > 0)
        {
            chip_group_user_selected_addOn.removeAllViews()
            for (AddonModel in Common.foodSelected!!.userSelectedAddon!!)
            {
                val chip = layoutInflater.inflate(R.layout.layout_chip_with_delete,null,false) as Chip
                chip.text =StringBuilder(AddonModel.name!!).append("(+$").append(AddonModel.price).append(")").toString()
                chip.isClickable =false
                chip.setOnCloseIconClickListener{view ->
                    chip_group_user_selected_addOn.removeView(view)
                    Common.foodSelected!!.userSelectedAddon!!.remove(AddonModel)
                    calculateTotalPrice()
                }
                chip_group_user_selected_addOn.addView(chip)
            }
        }
        else
            chip_group_user_selected_addOn.removeAllViews()

     }

    private fun showDialogRating() {
        val builder =AlertDialog.Builder(requireContext())
        builder.setTitle("Rating our Food")
        builder.setMessage("make a review")

        val itemView=LayoutInflater.from(context).inflate(R.layout.layout_rating_comment,null)
        val ratingBar =itemView.findViewById<RatingBar>(R.id.ratingId)
        val edtComment =itemView.findViewById<EditText>(R.id.edtComment)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL"){dialogInterface, i ->  dialogInterface.dismiss() }
        builder.setPositiveButton("OK"){ dialogInterface , i ->

            val commentModel= CommentModel()
            
            commentModel.name = Common.currentUser!!.name
            commentModel.uid = Common.currentUser!!.uid
            commentModel.comment = edtComment.text.toString()
            commentModel.ratingValue = ratingBar.rating
            
            val serverTimeStamp =HashMap<String,Any>()
            serverTimeStamp["timeStamp"] = ServerValue.TIMESTAMP
            commentModel.commentTimeStamp = (serverTimeStamp)
            foodDetailViewModel.setCommentModel(commentModel)
            }
        val dialog =builder.create()
        dialog.show()
     }


}




