package com.example.snsapplication.navigation.model

data class FollowDTO(
    var followerCount : Int = 0,
    //중복 follow 금지하기 위해
    var followers : MutableMap<String, Boolean> = HashMap(),
    var followingCount : Int = 0,
    var followings : MutableMap<String, Boolean> = HashMap()
)