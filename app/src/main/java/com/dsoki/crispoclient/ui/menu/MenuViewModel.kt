package com.dsoki.crispoclient.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.CallBack.ICategoryCallBackListener
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.model.BestDealsModel
import com.dsoki.crispoclient.model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuViewModel : ViewModel(), ICategoryCallBackListener {
    private var categoriesListMutable: MutableLiveData<List<CategoryModel>>? = null
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var categoryCallBackListener: ICategoryCallBackListener
    override fun onCategoryrLoadSuccess(categoriesList: List<CategoryModel>) {
        categoriesListMutable!!.value = categoriesList
    }

    override fun onCategoryLoadFail(message: String) {
        errorMessage!!.value = message
    }

    init {
        categoryCallBackListener = this
    }

    fun getCategoryList(): MutableLiveData<List<CategoryModel>> {
        if (categoriesListMutable == null) {
            categoriesListMutable = MutableLiveData()
            loadCategory()
         }
        return categoriesListMutable!!
    }
    fun getMessageError():MutableLiveData<String>{return errorMessage}

    private fun loadCategory() {
        val tempList =ArrayList<CategoryModel>()
        val categoriesRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORIES_REF)
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                categoryCallBackListener.onCategoryLoadFail(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children)
                {
                    val model =itemSnapShot.getValue<CategoryModel>(CategoryModel::class.java)
                    if (model != null) {
                        model!!.menu_id=itemSnapShot.key
                        tempList.add(model!!)
                    }
                }
                categoryCallBackListener.onCategoryrLoadSuccess(tempList)
            }
        })
    }
}




