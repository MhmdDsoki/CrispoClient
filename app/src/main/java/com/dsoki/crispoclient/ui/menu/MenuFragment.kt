package com.dsoki.crispoclient.ui.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsoki.crispoclient.Adapter.MyCategoriesAdapter
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.Common.SpacesItemDecoraton
import com.dsoki.crispoclient.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*
import kotlinx.android.synthetic.main.nav_header_home.*

class MenuFragment : Fragment() {

    private lateinit var menuViewModel: MenuViewModel
    private lateinit var dialog:AlertDialog
    private lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter:MyCategoriesAdapter?=null
    private var recycler_menu:RecyclerView?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuViewModel =
            ViewModelProviders.of(this).get(MenuViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_category, container, false)
        initViews(root)
        menuViewModel.getMessageError().observe(viewLifecycleOwner, Observer{
            Toast.makeText(context,it,Toast.LENGTH_LONG).show()
        })
        menuViewModel.getCategoryList().observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            adapter = MyCategoriesAdapter(requireContext()!!,it)
            recycler_menu!!.adapter =adapter
            recycler_menu!!.layoutAnimation =layoutAnimationController
        })
        return root
    }

    private fun initViews(root:View) {
        dialog =SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController=AnimationUtils.loadLayoutAnimation(context,R.anim.layout_animation_up_to_down)
        recycler_menu =root.findViewById(R.id.recycler_menu) as RecyclerView
        recycler_menu!!.setHasFixedSize(true)
        val layoutManger=GridLayoutManager(context,2)
        layoutManger.orientation =RecyclerView.VERTICAL
        layoutManger.spanSizeLookup=object :GridLayoutManager.SpanSizeLookup()
        {
            override fun getSpanSize(position: Int): Int {
                return if(adapter != null)
                {
                    when(adapter!!.getItemViewType(position))
                    {
                        Common.DEFAULT_COLUMN_COUNTS ->1
                        Common.FULL_WIDTH_COLUMN->2
                        else -> -1
                    }
                }else
                    -1
            }
        }
        recycler_menu!!.layoutManager =layoutManger
        recycler_menu!!.addItemDecoration(SpacesItemDecoraton(0))
    }
}
