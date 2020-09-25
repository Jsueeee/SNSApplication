package com.example.snsapplication.navigation.model

import android.app.Notification

data class PushDTO(
    var to : String? = null, //푸쉬를 받는 사람의 토큰 아이디
    var notification : Notification = Notification()
){
    data class Notification(
        var body : String? = null, //푸쉬 메시지의 주내용
        var title : String? = null //푸쉬 메시지의 제목
    )
}