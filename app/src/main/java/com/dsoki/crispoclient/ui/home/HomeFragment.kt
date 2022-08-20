package com.dsoki.crispoclient.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.asksira.loopingviewpager.LoopingViewPager
import com.dsoki.crispoclient.Adapter.MyBestDealsAdapter
import com.dsoki.crispoclient.Adapter.MyPopularCategoriesAdapter
import com.dsoki.crispoclient.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

  private lateinit var homeViewModel: HomeViewModel
    //@BindView(R.id.Recycler_popular)
    var recyclerView:RecyclerView?=null
    var viewPager:LoopingViewPager?=null
    var layoutAnimationController:LayoutAnimationController?=null
    var unbinder:Unbinder?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        initView(root)
        //Bind view
        homeViewModel.popularList.observe(viewLifecycleOwner, Observer {
            val listData=it
            val adapter =MyPopularCategoriesAdapter(requireContext(),listData)
            recyclerView!!.adapter=adapter
            recyclerView!!.layoutAnimation =layoutAnimationController
        })
        homeViewModel.bestDealsList.observe(viewLifecycleOwner, Observer {
            val adapter =MyBestDealsAdapter(requireContext(),it,false)
            viewPager!!.adapter=adapter
        })
        return root
    }

    private fun initView(root:View) {
        layoutAnimationController =AnimationUtils.loadLayoutAnimation(context,R.anim.layout_animation_up_to_down)
        viewPager =root.findViewById(R.id.viewPager) as LoopingViewPager
        recyclerView =root.findViewById(R.id.Recycler_popular) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager =LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)


    }

    override fun onPause() {
        super.onPause()
        viewPager!!.pauseAutoScroll()
    }

    override fun onResume() {
        super.onResume()
        viewPager!!.resumeAutoScroll()
    }
}
