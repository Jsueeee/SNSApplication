package com.example.snsapplication.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.snsapplication.LoginActivity
import com.example.snsapplication.MainActivity
import com.example.snsapplication.R
import com.example.snsapplication.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment(){

    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null//내 계정인지 상대방 계정인지 알기 위함

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid") //이전 화면에서 넘어온 값을 받아옴
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if(uid == currentUserUid){
            //MyPage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)//내 계정이면 signout 버튼 누르면 signout 되게
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                //액티비티 종료
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))//로그인 액티비티 호출
                auth?.signOut()
            }
        }else{
            //OtherUserPage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)//다른 사람 계정이면 signout 버튼이 follow 버튼으로 보이게
            //누구의 유저페이지인지 보여주는 텍스트뷰와 백버튼
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")//arguments에 있는 userId 값 세팅
            mainactivity?.toolbar_btn_back?.setOnClickListener{
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            mainactivity?.toolbar_title_image?.visibility = View.GONE //툴바 이미지 숨기기
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)//칸에 3개씩 뜰 수 있도록
        return fragmentView
    }
    //recyclerview에 사용할 adapter
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf() //contentDTOs를 담을 array 선언
        init {
            //생성자 선언, 데이터 베이스에 있는 값들을 읽어온다.
            //uid값이 내 uid일 때만 읽어오도록 쿼리 만들기
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //프로그램의 안정성을 위해서 쿼리의 스냅샷이 null일 경우 바로 종료
                if(querySnapshot == null)   return@addSnapshotListener

                //Get Data
                //querySnapshot.documents을 snapshot 안에 담아줌
                for(snapshot in querySnapshot.documents){
                    //snapshot을 contentDTO로 캐스팅한 다음에 contentDTOs에 담아줌
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged() //리사이클러뷰가 새로고침될 수 있도록
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            //화면의 폭을 먼저 가져오기
            var width = resources.displayMetrics.widthPixels /3  //폭의 1/3 값

            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview) // create class -> userfragmentrecyclerviewadapter select
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            //데이터 맵핑
            var imageview = (holder as CustomViewHolder).imageview //CustomViewHolder -> 여러 개 중 userfragment 꺼
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUri).apply(RequestOptions().centerCrop()).into(imageview)
        }

    }
}