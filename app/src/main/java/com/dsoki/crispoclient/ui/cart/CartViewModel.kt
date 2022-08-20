package com.dsoki.crispoclient.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Database.CartDataSource
import com.dsoki.crispoclient.Database.CartDatabase
import com.dsoki.crispoclient.Database.CartItem
import com.dsoki.crispoclient.Database.LocalCartDateSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel :ViewModel() {
    private val compositeDisposable : CompositeDisposable
    private var cartDataSource: CartDataSource?=null
    private var MutableLiveDataCartItem:MutableLiveData<List<CartItem>>?=null

    init {
        compositeDisposable = CompositeDisposable()
         }
    fun initCartDataSource(context: Context)
    {
        cartDataSource=LocalCartDateSource(CartDatabase.getInstance(context).cartDao())
    }
    fun getMutableLiveDataCartItems():MutableLiveData<List<CartItem>> {
        if (MutableLiveDataCartItem == null)
            MutableLiveDataCartItem = MutableLiveData()
            getCartItems()
            return MutableLiveDataCartItem!!
    }

    private fun getCartItems()
    {
        compositeDisposable.addAll(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({cartItems ->
                MutableLiveDataCartItem!!.value =cartItems
            },{t:Throwable?-> MutableLiveDataCartItem!!.value=null}))
    }
    fun onStop()
    {
        compositeDisposable.clear()
    }
}