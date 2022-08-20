package com.dsoki.crispoclient.ui.fooddetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.model.CommentModel
import com.dsoki.crispoclient.model.FoodModel

class FoodDetailViewModel :ViewModel(){
    //viewModel has no info about the view(activity or fragment).
   // but vew should interact with the view model and notify about diff actions
    //view has a reference about view model but viewModel NO .
    private var mutableLiveDataFood: MutableLiveData<FoodModel>?=null
    private var mutableLiveDataComment:MutableLiveData<CommentModel>?=null

    init {
        mutableLiveDataComment = MutableLiveData()
    }

    fun getMutablieLifeDataDetailFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood ==null)
            mutableLiveDataFood = MutableLiveData()
        mutableLiveDataFood!!.value = Common.foodSelected
        return mutableLiveDataFood!!
    }
    
    fun getMutablieLifeDatacomment(): MutableLiveData<CommentModel> {
        if (mutableLiveDataComment ==null)
            mutableLiveDataComment = MutableLiveData()
        return mutableLiveDataComment!!
    }
    fun setCommentModel(commentModel: CommentModel)
    {
        if (mutableLiveDataComment!= null)
            mutableLiveDataComment!!.value=(commentModel)
    }
    fun setFoodModel(foodModel:FoodModel?){
        mutableLiveDataFood!!.value =foodModel
    }
}