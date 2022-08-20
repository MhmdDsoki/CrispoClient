package com.dsoki.crispoclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsoki.crispoclient.CallBack.IRecyclerViewItemClickListener
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.CartItem
import com.dsoki.crispoclient.Database.LocalCartDateSource
import com.dsoki.crispoclient.EventBus.CountCartEvent
import com.dsoki.crispoclient.EventBus.FoodClick
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.FoodModel
import io.reactivex.Scheduler
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import org.greenrobot.eventbus.EventBus

class MyFoodListAdapter (internal var context: Context,internal var foodList: List<FoodModel>):
    RecyclerView.Adapter<MyFoodListAdapter.MyViwHolder>(){
    private val compositeDisposable :CompositeDisposable
    private val cartDataSource:CartDataSource
    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource =LocalCartDateSource(CartDatabase.getInstance(context).cartDao())
    }
    inner class MyViwHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }
        
        var food_name : TextView?=null
        var food_price : TextView?=null
        var food_cart: ImageView?=null
        var food_fav : ImageView?=null
        var food_image : ImageView?=null
        internal var listener: IRecyclerViewItemClickListener?=null

        fun setListener(listener: IRecyclerViewItemClickListener)
        {
            this.listener =listener
        }
        init {
            food_name =itemView.findViewById(R.id.txt_food_name) as TextView
            food_price =itemView.findViewById(R.id.txt_food_price) as TextView
            food_cart =itemView.findViewById(R.id.food_cart) as ImageView
            food_fav =itemView.findViewById(R.id.food_fav) as ImageView
            food_image =itemView.findViewById(R.id.food_image) as ImageView
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyFoodListAdapter.MyViwHolder {
        return MyViwHolder(LayoutInflater.from(context).inflate(R.layout.food_list_item,parent,false))
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: MyViwHolder, position: Int) {
        Glide.with(context).load(foodList.get(position).image).into(holder.food_image!!)
        holder.food_price!!.setText(foodList.get(position).price.toString())
        holder.food_name!!.setText(foodList.get(position).name)
        //Event
        holder.setListener(object : IRecyclerViewItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected=foodList.get(pos)
                Common.foodSelected!!.key =pos.toString()
                EventBus.getDefault().postSticky(FoodClick(true,foodList.get(pos)))
            }
        })
        holder.food_cart!!.setOnClickListener {
            val cartItem =CartItem()
            cartItem.uid=Common.currentUser!!.uid
            cartItem.userPhone=Common.currentUser!!.phone

            cartItem.foodId = foodList.get(position).id
            cartItem.foodName = foodList.get(position).name
            cartItem.foodImage = foodList.get(position).image
            cartItem.foodPrice = foodList.get(position).price!!.toDouble()

            cartItem.foodQuantity=1
            cartItem.foodExtraPrice=0.0
            cartItem.foodAddon="Default"
            cartItem.foodSize ="Default"

            cartDataSource.getItemWithAllOptionsInCart(Common.currentUser!!.uid!!,
                cartItem.foodId!!.toString(),
                cartItem.foodSize!!,
                cartItem.foodAddon!!).
            subscribeOn(Schedulers.io()).
            observeOn(AndroidSchedulers.mainThread()).
            subscribe(object : SingleObserver<CartItem>{
                    override fun onSuccess(cartItemFromDB: CartItem) {

                        if (cartItemFromDB.equals(cartItem))
                        {   //if item already in db just update.
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
                    override fun onSubscribe(d: Disposable) {
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

    fun onStop() {
        compositeDisposable.clear()
    }


}