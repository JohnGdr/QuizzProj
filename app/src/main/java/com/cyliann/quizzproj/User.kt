package com.cyliann.quizzproj

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val pseudo: String? = null,
    val email: String? = null,
    val friendsList: List<String> = listOf(),
    val pp: String? = null,
    var uid: String? = null,
    var totalscore: Int = 0
){
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot
}