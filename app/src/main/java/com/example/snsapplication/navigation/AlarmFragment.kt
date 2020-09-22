package com.example.snsapplication.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.snsapplication.R
import com.example.snsapplication.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*

class AlarmFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)
        //리사이클러뷰를 리프레쉬
        view.alarmframent_recyclerview.adapter = AlarmRecyclerviewAdapter()
        view.alarmframent_recyclerview.layoutManager = LinearLayoutManager(activity) //세로로 배치

        return view
    }
    //어댑터
    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        //알람 저장하는 리스트 변수
        var alarmDTOList : ArrayList<AlarmDTO> = arrayListOf()

        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid//현재 유저의 uid

            //db에서 쿼리를 이용, 나에게 도착한 메시지만 필터링
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if(querySnapshot == null) return@addSnapshotListener //안정성

                for(snapshot in querySnapshot.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView //종류에 따라 메시지를 다르게 표시할 수 있도록

            //DB에 접근, 프로필 이미지 가져오기
            //상대방 uid 넣어주면 상대방 이미지 주소를 받아올 수 있음
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result!!["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                }
            }

            when(alarmDTOList[position].kind){
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.commentviewitem_textview_profile.text = str_0
                }
                1 -> {
                    val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.commentviewitem_textview_profile.text = str_0
                }
                2 -> {
                val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                view.commentviewitem_textview_profile.text = str_0
                }

            }
            view.commentviewitem_textview_comment.visibility = View.INVISIBLE
        }

    }
}