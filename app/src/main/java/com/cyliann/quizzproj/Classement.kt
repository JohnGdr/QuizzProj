package com.cyliann.quizzproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query

class Classement: BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var listAdapter: ClassementAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profilePic: ImageView
    private lateinit var pseudoUnderPP: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classement)

        listAdapter = ClassementAdapter(this, arrayOf())
        supportActionBar?.hide()


        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)


        val listView = findViewById<ListView>(R.id.listViewUser)
        listView.adapter = listAdapter

        getUsersScore()

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.z = -1F
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                drawerLayout.z = 1F
            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.z = -1F
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun getUsersScore(){
        val users = mutableListOf<User>()
        db.collection("user")
            .orderBy("totalscore", Query.Direction.DESCENDING) // Trie les documents par totalScore en ordre décroissant
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("HELLO", "${document.id} => ${document.data}")
                    val user = document.toObject(User::class.java)
                    user.uid = document.id
                    users.add(user)
                }
                listAdapter.setList(users.toTypedArray())
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting documents: ", exception)
            }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, ChoixQuizz::class.java)
                finish()
                startActivity(intent)
            }
            R.id.nav_profil -> {
                if(auth.currentUser != null){
                    val intent = Intent(this, Profil()::class.java)
                    intent.putExtra("isUser", true)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_settings -> {

            }
            R.id.nav_friends -> {
                if(auth.currentUser != null){
                    val intent = Intent(this, Friends::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_ppChange -> {
                if (auth.currentUser != null)
                    showChangePPDialog(profilePic, pseudoUnderPP)
                else Toast.makeText(this, "Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                if(auth.currentUser != null) {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else{
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            R.id.nav_myQuizz -> {
                if(auth.currentUser != null){
                    val intent = Intent(this, CreatedQuizzList()::class.java)
                    intent.putExtra("isUser", true)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_classement -> {
                val intent = Intent(this, Classement()::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}