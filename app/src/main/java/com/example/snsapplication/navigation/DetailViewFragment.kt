package com.example.snsapplication.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snsapplication.R
import com.example.snsapplication.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){

    //DB에 접근할 수 있도록
    var firestore : FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        //onCreateView 안에서 초기화 해주어야 함.
        firestore = FirebaseFirestore.getInstance()

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapger()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity) //화면 세로 배치
        return view
    }
    inner class DetailViewRecyclerViewAdapger : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs : ArrayList<ContentDTO> = arrayListOf() //contentDTO를 담을 수 있는 arryList 생성
        var contentUidList : ArrayList<String> = arrayListOf()
        init{
            //생성자 만들기

            //DB에 접근을 해서 데이터를 받아주는 쿼리 만들기 //시간순으로 받아오기
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()//받자마자 contentDTOs 값 초기화
                contentUidList.clear()
                //for문 돌려서 snapshot에 들어오는 데이터 하나씩 읽음
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java) //캐스팅
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                //값이 새로고침되도록
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomviewHolder(view)//create class - detailviewrecyclerviewadapter
        }

        inner class CustomviewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomviewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![p1].userId //contentDTOs에 userId를 매핑 //contentDTOs는 array이기 때문에 position 값을 넣어줘서 받아와야 함.

            //Image 매핑
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUri).into(viewholder.detailviewitem_imageview_content)

            //Explain 매핑
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

            //like counter 매핑
            viewholder.detailviewitem_favoritecounter_textview. text = "Likes " + contentDTOs!![p1].favoriteCount

            //ProfileImage 매핑
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUri).into(viewholder.detailviewitem_profile_image)
        }
    }
}