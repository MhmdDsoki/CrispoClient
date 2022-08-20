package com.dsoki.crispoclient.CallBack

import com.dsoki.crispoclient.model.PopularCategoryModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularCategoryList: List<PopularCategoryModel>)
    fun onPopularLoadFail(message:String)
}