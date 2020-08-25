package com.example.snsapplication.navigation.model

data class ContentDTO(var explain : String? = null,
                      var imageUri : String? = null,
                      var uid : String? = null,
                      var userId : String? = null,
                      var timestamp : Long? = null,
                      var favoriteCount : Int = 0,
                      //중복 좋아요 방지할 수 있도록 유저 정보 관리
                      var favorites : MutableMap<String, Boolean> = HashMap()){
    //댓글 관리
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null)
}