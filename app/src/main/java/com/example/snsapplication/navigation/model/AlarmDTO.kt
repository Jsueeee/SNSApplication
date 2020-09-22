package com.example.snsapplication.navigation.model

data class AlarmDTO(
    var destinationUid : String? = null,
    var userId : String? = null,
    var uid : String? = null,
    var kind : Int? = null, //어떤 타입의 메시지인지
    var message : String? = null,
    var timestamp : Long? = null
)