package com.cyliann.quizzproj

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Quizz(
    var id: String?=null,
    val Titre: String?=null,
    val Createur: String?=null,
    val Tags: List<String> = listOf() ,
    val Questions : HashMap<String, HashMap<String, Boolean>> = hashMapOf(),
    var pseudoCreateur: String?=null
) {
}