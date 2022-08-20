package com.dsoki.crispoclient.CallBack

import com.dsoki.crispoclient.model.BestDealsModel
import com.dsoki.crispoclient.model.PopularCategoryModel

interface IBestDealsLoadCallback {
    fun onBestDealsLoadSuccess(besDealsList: List<BestDealsModel>)
    fun onBestDealsLoadFail(message:String)
}