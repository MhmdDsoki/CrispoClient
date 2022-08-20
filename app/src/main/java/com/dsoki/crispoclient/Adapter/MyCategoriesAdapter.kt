package com.dsoki.crispoclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dsoki.crispoclient.CallBack.IRecyclerViewItemClickListener
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.EventBus.CategoryClick
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.CategoryModel
import org.greenrobot.eventbus.EventBus

class MyCategoriesAdapter (internal var context: Context, internal var categoriesList: List<CategoryModel>):
    RecyclerView.Adapter<MyCategoriesAdapter.MyViwHolder>(){
    inner class MyViwHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var categry_name : TextView?=null
        var category_image : ImageView?=null
        internal var listener: IRecyclerViewItemClickListener?=null
        fun setListener(listener:IRecyclerViewItemClickListener)
        {
            this.listener =listener
        }
        init {
            categry_name =itemView.findViewById(R.id.category_name) as TextView
            category_image =itemView.findViewById(R.id.category_iamge_view) as ImageView
            itemView.setOnClickListener(this)
        }
        override fun onClick(view: View?) {
                listener!!.onItemClick(view!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyCategoriesAdapter.MyViwHolder {
        return MyViwHolder(LayoutInflater.from(context).inflate(R.layout.category_item,parent,false))
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: MyViwHolder, position: Int) {
        Glide.with(context).load(categoriesList.get(position).image).into(holder.category_image!!)
        holder.categry_name!!.setText(categoriesList.get(position).name)
        //Event
        holder.setListener(object :IRecyclerViewItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                Common.categorySelected=categoriesList.get(pos)
                EventBus.getDefault().postSticky(CategoryClick(true,categoriesList.get(pos)))
            }

        })
    }

    override fun getItemViewType(position: Int): Int {
        return if(categoriesList.size ==1)
            Common.DEFAULT_COLUMN_COUNTS
        else {
            if(categoriesList.size%2==0)
                Common.DEFAULT_COLUMN_COUNTS
            else
                if (position>1&&position==categoriesList.size)
                    Common.FULL_WIDTH_COLUMN
                else Common.DEFAULT_COLUMN_COUNTS
             }
        }
    }