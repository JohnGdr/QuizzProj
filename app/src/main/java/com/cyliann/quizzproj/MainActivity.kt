package com.cyliann.quizzproj

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val signupText = findViewById<TextView>(R.id.signupText)
        val loginButton = findViewById<Button>(R.id.connectButton)
        val guestButton = findViewById<Button>(R.id.guestButton)

        loginButton.setOnClickListener{
            val login = findViewById<EditText>(R.id.EditText).text.toString()
            val mdp = findViewById<EditText>(R.id.EditText2).text.toString()
            if (mdp.isNotBlank() && login.isNotBlank()){
                loginUser(login, mdp)
            }
            else{
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        guestButton.setOnClickListener(){
            startActivity(Intent(this, ChoixQuizz::class.java))
        }

        signupText.setOnClickListener() {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        supportActionBar?.hide()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val i = Intent(this, ChoixQuizz::class.java)
            startActivity(i)
        }

        val content = SpannableString("S'inscrire")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)

        // Appliquer la SpannableString au TextView
        signupText.setText(content)
    }



    override fun onResume() {
        super.onResume()
        if(auth.currentUser != null){
            startActivity(Intent(this, ChoixQuizz::class.java))
        }
    }

}