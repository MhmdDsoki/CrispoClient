package com.dsoki.crispoclient.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bumptech.glide.Glide
import com.dsoki.crispoclient.CallBack.IRecyclerViewItemClickListener
import com.dsoki.crispoclient.EventBus.PopularFoodItemClick
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.PopularCategoryModel
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_popular_categories_item.view.*
import org.greenrobot.eventbus.EventBus

class MyPopularCategoriesAdapter (internal var context: Context,
                                  internal var popularCategoryModel: List<PopularCategoryModel>):
                                  RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViwHolder>(){
    inner class MyViwHolder(itemView: View): RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(p0: View?) {
            listener!!.onItemClick(p0!!,adapterPosition)
        }

        var txt_categry_name :TextView?=null
        var category_image :CircleImageView?=null
        internal var listener: IRecyclerViewItemClickListener?=null

        fun setListener(listener: IRecyclerViewItemClickListener)
        {
            this.listener =listener
        }
        init {
            txt_categry_name =itemView.findViewById(R.id.txt_category_name) as TextView
            category_image =itemView.findViewById(R.id.category_image) as CircleImageView
            itemView.setOnClickListener(this)
             }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViwHolder {
        return MyViwHolder(LayoutInflater.from(context).inflate(R.layout.layout_popular_categories_item,parent,false))
    }

    override fun getItemCount(): Int {
          return popularCategoryModel.size
    }

    override fun onBindViewHolder(holder: MyViwHolder, position: Int) {
        Glide.with(context).load(popularCategoryModel.get(position).image).into(holder.category_image!!)
        holder.txt_categry_name!!.setText(popularCategoryModel.get(position).name)
        holder.setListener(object : IRecyclerViewItemClickListener{
        override fun onItemClick(view: View, pos: Int) {
                EventBus.getDefault().postSticky(PopularFoodItemClick(popularCategoryModel[pos]))
        }
     })
   }
}