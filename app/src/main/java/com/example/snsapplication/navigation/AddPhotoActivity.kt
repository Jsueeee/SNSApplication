package com.example.snsapplication.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.snsapplication.R
import com.example.snsapplication.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        //Initate Storage
        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        //add image upload event
        addPhoto_btn_upload.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(requestCode == Activity.RESULT_OK){
                //사진을 선택했을 때 이미지의 경로가 여기로 넘어옴
                photoUri = data?.data //photoUri에 경로 담아주기
                addPhoto.setImageURI(photoUri) //이미지뷰에 선택한 이미지 보여주기

            }else{
                //취소 버튼 눌렀을 때
                finish()
            }
        }
    }
    fun contentUpload(){
        //make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child(imageFileName)

        /*
        업로드 방식에는 두가지가 있다.
        1. Promise method
        2. Callback method
        */



        //Callback method
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            //이미지 업로드가 완료 됐으면 이미지 주소를 받아온다
            storageRef?.putFile(photoUri!!)?.addOnSuccessListener { uri->
                //데이터모델 만들기
                var contentDTO = ContentDTO()

                //Insert downloadUrl of image
                contentDTO.imageUri = uri.toString() //content url을 넣어줌

                //Insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                //Insert userId
                contentDTO.userId = auth?.currentUser?.email

                //Insert explain of content
                contentDTO.explain = addPhoto_edit_explain.text.toString()

                //Insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK) //정상적으로 닫혔다는 flag를 넘겨줌

                finish()
            }
        }
    }
}