package com.dsoki.crispoclient.ui.foodList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsoki.crispoclient.Adapter.MyFoodListAdapter
import com.dsoki.crispoclient.Common.Common.categorySelected
import com.dsoki.crispoclient.R

class FoodListFragment : Fragment() {

    private lateinit var foodListViewModel: FoodListViewModel
    var recycler_food_list:RecyclerView?=null
    var layoutAnimationController:LayoutAnimationController?=null
    var adapter : MyFoodListAdapter ?=null

    override fun onStop() {
        if (adapter != null)
             adapter!!.onStop()
        super.onStop()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel = ViewModelProviders.of(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_foodlist, container,false)
        initViews(root)
        foodListViewModel.getMutableFoodListModelData().observe(viewLifecycleOwner, Observer {
        adapter = MyFoodListAdapter(requireContext(),it)
        recycler_food_list!!.adapter=adapter
        recycler_food_list!!.layoutAnimation=layoutAnimationController
        })
        return root
    }


    private fun initViews(root: View?) {
        recycler_food_list = root!!.findViewById(R.id.recycler_food_list) as RecyclerView
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager= LinearLayoutManager(context)
        layoutAnimationController =AnimationUtils.loadLayoutAnimation(context,R.anim.layout_animation_up_to_down)
        (activity as AppCompatActivity).supportActionBar!!.title =categorySelected!!.name
    }
}
