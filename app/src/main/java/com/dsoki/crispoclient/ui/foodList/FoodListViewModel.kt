package com.dsoki.crispoclient.ui.foodList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.model.FoodModel

class FoodListViewModel : ViewModel() {
    private var mutableFoodListLiveData:MutableLiveData<List<FoodModel>>?=null
    fun getMutableFoodListModelData():MutableLiveData<List<FoodModel>>{
        if (mutableFoodListLiveData==null)
            mutableFoodListLiveData=MutableLiveData()
            mutableFoodListLiveData!!.value=Common.categorySelected!!.foods
        return mutableFoodListLiveData!!
    }

}