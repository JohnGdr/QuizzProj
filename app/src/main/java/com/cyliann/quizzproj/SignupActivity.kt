package com.cyliann.quizzproj

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class SignupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.hide()
    }

    fun onFinishSignupClick(v: View){
        val mail = findViewById<EditText>(R.id.EditText).text.toString()
        val mdp = findViewById<EditText>(R.id.EditText2).text.toString()
        val pseudo = findViewById<EditText>(R.id.pseudo).text.toString()
        if (pseudo.isNotBlank() && mdp.isNotBlank() && mail.isNotBlank()) {
            registerUser(pseudo, mail, mdp)
        }
        else{
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }
}