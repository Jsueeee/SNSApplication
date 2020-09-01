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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class DetailViewFragment : Fragment(){

    //DB에 접근할 수 있도록
    var firestore : FirebaseFirestore? = null
    var uid : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        //onCreateView 안에서 초기화 해주어야 함.
        firestore = FirebaseFirestore.getInstance()
        var uid = FirebaseAuth.getInstance().currentUser?.uid

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
                //sign out을 누르면 크러쉬 발생 -> firestore의 스냅샷에서 에러 -> 아래 코드 추가로 안전성 높임
                if(querySnapshot == null)   return@addSnapshotListener
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

            //Like 버튼에 이벤트 달아주기
            viewholder.detailviewitem_favorit_imageview.setOnClickListener{
                favoriteEvent(p1)
            }
            //내 uid가 포함되어 있을 경우
            if (contentDTOs!![p1].favorites.containsKey(uid)){
                //좋아요 버튼이 클릭된 경우
                //꽉 찬 하트
                viewholder.detailviewitem_favorit_imageview.setImageResource(R.drawable.ic_favorite)
            }else{
                //포함되지 않을 경우
                //좋아요 버튼이 클릭되지 않은 경우
                //비어있는 하트
                viewholder.detailviewitem_favorit_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //프로필 이미지 클릭하면 상대방 유저 정보로 이동
            viewholder.detailviewitem_profile_image.setOnClickListener{
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid", contentDTOs[p1].uid)
                bundle.putString("userId", contentDTOs[p1].userId)//이메일값
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }
        }
        fun favoriteEvent(position : Int){
            //내가 선택한 컨텐츠의 uid를 받아와서 좋아요해주는 이벤트
            //document안에 내가 선택한 uid 값을 넣어주면 됨.
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            //데이터 입력 위한 트랜잭션 불러오기
            firestore?.runTransaction { transaction ->
                var uid = FirebaseAuth.getInstance().currentUser?.uid
                //트랜잭션의 데이터를 contentDTO로 캐스팅
                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                //좋아요 버튼 이미 클릭되어 있을 경우/ 아닌 경우 구분해주기
                if(contentDTO!!.favorites.containsKey(uid)){
                    //버튼 클릭을 취소하기
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount -1
                    contentDTO?.favorites.remove(uid)

                }else{
                    //버튼 클릭
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount +1
                    contentDTO?.favorites[uid!!] = true
                }
                //트랜잭션을 다시 서버로 돌려주기
                transaction.set(tsDoc, contentDTO)
            }
        }
    }
}