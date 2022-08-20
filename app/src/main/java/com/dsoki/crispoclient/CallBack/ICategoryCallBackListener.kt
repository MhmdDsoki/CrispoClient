package com.dsoki.crispoclient.CallBack

import com.dsoki.crispoclient.model.BestDealsModel
import com.dsoki.crispoclient.model.CategoryModel
import com.dsoki.crispoclient.model.PopularCategoryModel

interface ICategoryCallBackListener {
    fun onCategoryrLoadSuccess(categoriesList: List<CategoryModel>)
    fun onCategoryLoadFail(message:String)
}