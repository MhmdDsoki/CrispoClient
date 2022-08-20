package com.dsoki.crispoclient.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dsoki.crispoclient.model.CommentModel

class CommentViewModel : ViewModel() {
    var mutableLiveDataCommentList:MutableLiveData<List<CommentModel>>?=null
    init{
        mutableLiveDataCommentList = MutableLiveData()
        }
fun setCommentList(commentList:List<CommentModel>)
{
    mutableLiveDataCommentList!!.value =commentList
}
}