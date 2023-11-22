package com.cyliann.quizzproj
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth


class ChoixQuizz : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choix_quizz)
        val quizz_list = mutableListOf<Quizz>()
        val listAdapter = ChoixQuizzAdapter(this, quizz_list.toTypedArray())
        supportActionBar?.hide()
        db.collection("quizz")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("HELLO", "${document.id} => ${document.data}")
                    val quizz = Quizz(document.data.get("Titre").toString(),document.id)
                    quizz_list.add(quizz)
                }
                listAdapter.setList(quizz_list.toTypedArray())
            }
            .addOnFailureListener { exception ->
                Log.d("Hello", "Error getting documents: ", exception)
            }
        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)


        val listView = findViewById<ListView>(R.id.listViewQuizz)
        listView.adapter = listAdapter


    }


    override fun onBackPressed() {
        super.onBackPressed()
        FirebaseAuth.getInstance().signOut()
    }

}