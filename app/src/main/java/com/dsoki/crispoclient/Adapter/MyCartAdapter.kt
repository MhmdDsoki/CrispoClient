package com.dsoki.crispoclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.CartItem
import com.dsoki.crispoclient.Database.LocalCartDateSource
import com.dsoki.crispoclient.EventBus.updateItemsInCart
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.CategoryModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.layout_cart_item.view.*
import org.greenrobot.eventbus.EventBus

class MyCartAdapter ( internal var context: Context,internal var cartItem: List<CartItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyViwHolder>(){

    internal var compositionalDisposal:CompositeDisposable
    internal var cartDateSource:CartDataSource

    init {
        compositionalDisposal= CompositeDisposable()
        cartDateSource=LocalCartDateSource(CartDatabase.getInstance(context).cartDao())
    }

    inner class MyViwHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
         var img_cart:ImageView ?=null
         var txt_food_name:TextView ?=null
         var txt_food_price:TextView ?=null
         var number_button:ElegantNumberButton ?=null

        init {
                img_cart=itemView.findViewById(R.id.img_cart) as ImageView
                txt_food_name=itemView.findViewById(R.id.txt_food_item) as TextView
                txt_food_price=itemView.findViewById(R.id.txt_food_price) as TextView
                number_button=itemView.findViewById(R.id.number_button) as ElegantNumberButton
             }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViwHolder {
        return MyViwHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))    }

    override fun onBindViewHolder(holder: MyViwHolder, position: Int) {
        Glide.with(context).load(cartItem[position].foodImage).into(holder.img_cart!!)
        holder.txt_food_name !!.text =StringBuilder(cartItem[position].foodName!!)
        holder.number_button!!.number =cartItem[position].foodQuantity.toString()
        holder.txt_food_price!!.text = StringBuilder("").append( cartItem[position].foodPrice!! + cartItem[position].foodExtraPrice!! )
        //EVENT
        holder.number_button!!.setOnValueChangeListener{view , oldValue, newValue->
            cartItem[position].foodQuantity =newValue
            EventBus.getDefault().postSticky(updateItemsInCart(cartItem[position]))
        }
    }
    override fun getItemCount(): Int {
        return cartItem.size
    }
    fun getItemPosition(pos: Int): CartItem {
        return cartItem[pos]
    }
}