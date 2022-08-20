package com.dsoki.crispoclient.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.CallBack.IBestDealsLoadCallback
import com.dsoki.crispoclient.CallBack.IPopularLoadCallback
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.model.BestDealsModel
import com.dsoki.crispoclient.model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(),IPopularLoadCallback, IBestDealsLoadCallback {
    private  var popularListMutableLiveData: MutableLiveData<List<PopularCategoryModel>>?=null
    private  var bestDealsListMutableLiveData: MutableLiveData<List<BestDealsModel>>?=null
    private lateinit var messageError: MutableLiveData<String>
    private var popularLoadCallbackListener:IPopularLoadCallback
    private lateinit var bestDealsCallbackListener:IBestDealsLoadCallback

    val bestDealsList:LiveData<List<BestDealsModel>>
        get() {
            if (bestDealsListMutableLiveData==null)
            {
                bestDealsListMutableLiveData= MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealsList()
            }
            return bestDealsListMutableLiveData!!
        }

    private fun loadBestDealsList() {
        val tempList =ArrayList<BestDealsModel>()
        val bestDealsRef =FirebaseDatabase.getInstance().getReference(Common.BEST_REF)
        bestDealsRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                bestDealsCallbackListener.onBestDealsLoadFail(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children)
                {
                    val model =itemSnapShot.getValue<BestDealsModel>(BestDealsModel::class.java)
                    if (model != null) {
                        tempList.add(model)
                    }
                }
                bestDealsCallbackListener.onBestDealsLoadSuccess(tempList)
            }
        })
    }

    val popularList:LiveData<List<PopularCategoryModel>>
    get() {
        if (popularListMutableLiveData==null)
        {
            popularListMutableLiveData= MutableLiveData()
            messageError = MutableLiveData()
            loadPopularList()
        }
        return popularListMutableLiveData!!
    }

    private fun loadPopularList() {
        val tempList =ArrayList<PopularCategoryModel>()
        val popularRef =FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
               popularLoadCallbackListener.onPopularLoadFail(p0.message)
            }
            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children)
                {
                    val model =itemSnapShot.getValue<PopularCategoryModel>(PopularCategoryModel::class.java)
                    if (model != null) {
                        tempList.add(model)
                    }
                }
                popularLoadCallbackListener.onPopularLoadSuccess(tempList)
             }
        })
    }

    init {
        popularLoadCallbackListener=this
        bestDealsCallbackListener=this
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
      popularListMutableLiveData!!.value =popularModelList
    }

    override fun onPopularLoadFail(message: String) {
        messageError.value=message
    }

    override fun onBestDealsLoadSuccess(besDealsList: List<BestDealsModel>) {
        bestDealsListMutableLiveData!!.value =besDealsList   
        
    }

    override fun onBestDealsLoadFail(message: String) {
        messageError.value=message
    }
}