package com.dsoki.crispoclient.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.bumptech.glide.Glide
import com.dsoki.crispoclient.EventBus.BestDealsItemClick
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.BestDealsModel
import kotlinx.android.synthetic.main.layout_best_deals_item.view.*
import org.greenrobot.eventbus.EventBus

class MyBestDealsAdapter(context: Context,itemList:List<BestDealsModel>,isInfinit:Boolean ):
    LoopingPagerAdapter<BestDealsModel>(context,itemList,isInfinit) {
    override fun bindView(convertView: View, listPosition: Int, viewType: Int) {
        val imageView=convertView.findViewById<ImageView>(R.id.img_best_deal)
        val textView=convertView.findViewById<TextView>(R.id.txt_best_deals)
        //setData
        Glide.with(context).load(itemList?.get(listPosition)!!.image).into(imageView)
        textView.text=itemList?.get(listPosition)!!.name
        convertView.setOnClickListener {
            EventBus.getDefault().postSticky(BestDealsItemClick(itemList!![listPosition]))
        }
    }

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deals_item,container,false) }
}