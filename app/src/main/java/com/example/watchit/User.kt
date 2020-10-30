package com.example.watchit

import kotlinx.serialization.Serializable


@Serializable
data class User(var aditional_infos : String = "",
                var email : String = "",
                var first_name : String = "",
                var birthday : String = "",
                var id : Int = 0,
                var last_name : String = "",
                var password : String = "")
{

}

@Serializable
data class UserResponse(val id: Int)