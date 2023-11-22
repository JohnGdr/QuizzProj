package com.cyliann.quizzproj

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.TextView

class QuizzGame: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quizz_game)
        val docId = intent.getStringExtra("docId")
        Log.e("docId", docId.toString())

        db.collection("quizz")
            .document(docId!!).get()

        supportActionBar?.hide()

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)

        val titleView = findViewById<TextView>(R.id.titleView)
    }
}