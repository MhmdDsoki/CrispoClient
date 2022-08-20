package com.dsoki.crispoclient.Adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.CommentModel

class MyCommentAdapter (internal var context: Context, internal var commentList: List<CommentModel>):
                        RecyclerView.Adapter<MyCommentAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.show_comment_item,parent,false))
    }

    override fun getItemCount(): Int {
      return commentList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      val timestamp =commentList.get(position).commentTimeStamp!!["timeStamp"]!!.toString().toLong()
        holder.txtcommentDate!!.text= DateUtils.getRelativeTimeSpanString(timestamp)
        holder.txtcomment!!.text=commentList.get(position).comment
        holder.txtUserName!!.text=commentList.get(position).name
        holder.app_bar_layout!!.rating=commentList.get(position).ratingValue
    }
    inner class MyViewHolder(itemView: View):  RecyclerView.ViewHolder(itemView)
        {
            var txtUserName: TextView?=null
            var app_bar_layout: RatingBar?=null
            var txtcommentDate: TextView?=null
            var txtcomment: TextView?=null
           // var User_Image: ImageView?=null
            init
            {
                txtUserName =itemView.findViewById(R.id.txtUserName) as TextView
                txtcommentDate =itemView.findViewById(R.id.txtcommentDate) as TextView
                txtcomment =itemView.findViewById(R.id.txtcomment) as TextView
                app_bar_layout=itemView.findViewById(R.id.app_bar_layout) as RatingBar
            }

        }


}