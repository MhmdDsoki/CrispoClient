package com.dsoki.crispoclient.CallBack
import com.dsoki.crispoclient.model.CommentModel

interface ICommentCallBack {
    fun onCommentLoadSuccess(commentList: List<CommentModel>)
    fun onCommetLoadFail(message:String)
}