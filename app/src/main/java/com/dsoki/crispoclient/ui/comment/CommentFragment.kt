package com.dsoki.crispoclient.ui.comment
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dsoki.crispoclient.Adapter.MyCommentAdapter
import com.dsoki.crispoclient.CallBack.ICommentCallBack
import com.dsoki.crispoclient.Common.Common
import com.dsoki.crispoclient.R
import com.dsoki.crispoclient.model.CommentModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
class CommentFragment:BottomSheetDialogFragment(), ICommentCallBack {

    private var commentViewModel:CommentViewModel?=null
    private var listener:ICommentCallBack?=null
    private var recycler_comment:RecyclerView?=null
    private var dialog:AlertDialog?=null

    init {
            listener=this
         }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView =LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout_fragment,container,false)
        initViews(itemView)
        loadCommentFromFirebase()
        commentViewModel!!.mutableLiveDataCommentList!!.observe(this, Observer {  commentList ->
        val adapter =MyCommentAdapter(requireContext(),commentList)
            recycler_comment!!.adapter =adapter
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()
        val commentModels =ArrayList<CommentModel>()
        Common.COMMENT_REF?.let {
            FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.foodSelected!!.id!!)
                .orderByChild("commentTimeStamp").limitToLast(100)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        listener!!.onCommetLoadFail(error.message)
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                            for (commentSnapShot in snapshot.children)
                            {
                                val commentModel =commentSnapShot.getValue<CommentModel>(CommentModel::class.java)
                                commentModels.add(commentModel!!)
                            }
                        listener!!.onCommentLoadSuccess(commentModels)
                     }
                })
           }
     }

    private fun initViews(itemView: View?) {
    commentViewModel =ViewModelProviders.of(this).get(CommentViewModel::class.java)
    dialog =SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
    recycler_comment =itemView!!.findViewById(R.id.recycler_comments) as RecyclerView
    recycler_comment!!.setHasFixedSize(true)
    val layoutManager=LinearLayoutManager(context,RecyclerView.VERTICAL,true)
    recycler_comment!!.layoutManager=layoutManager
    recycler_comment!!.addItemDecoration(DividerItemDecoration(requireContext(),layoutManager.orientation))
    }

    override fun onCommentLoadSuccess(commentList: List<CommentModel>) {
        dialog!!.dismiss()
        commentViewModel!!.setCommentList(commentList)
    }

    override fun onCommetLoadFail(message: String) {
        Toast.makeText(requireContext(),""+message,Toast.LENGTH_LONG)
        dialog!!.dismiss()
     }
    companion object{
        private var instance:CommentFragment? =null
        fun getInstance():CommentFragment{
            if (instance==null)
                instance= CommentFragment()
            return instance!!
        }
    }
}