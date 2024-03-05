package com.cyliann.quizzproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class Profil: BaseActivity(), NavigationView.OnNavigationItemSelectedListener, OnClickListener {
    private lateinit var listAdapter: CreatedQuizzListAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profilePic: ImageView
    private lateinit var pseudoUnderPP: TextView
    private var isUser by Delegates.notNull<Boolean>()
    private lateinit var Id: String
    private lateinit var card : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)
        if (!intent.getStringExtra("docId").isNullOrEmpty()){
            Id = intent.getStringExtra("docId").toString()
            isUser = false
        }

        if ((intent.getBooleanExtra("isUser", false) == true)){
            isUser = true
        }

        card = findViewById<CardView>(R.id.add_friend_card)
        card.setOnClickListener {
            onClick(it)
        }

        if ((auth.currentUser == null) || (isUser)){
            card.visibility = View.INVISIBLE
            card.isEnabled = false
        }

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)

        supportActionBar?.hide()

        listAdapter = CreatedQuizzListAdapter(this, arrayOf(), null, this, auth)

        val listView = findViewById<ListView>(R.id.listViewQuizz)
        listView.adapter = listAdapter

        val pp = findViewById<ImageView>(R.id.profilePicProfil)
        val pseudo = findViewById<TextView>(R.id.pseudoUnderPPProfil)
        val score = findViewById<TextView>(R.id.score)

        if (isUser){
            loadUserInfo(pp, pseudo, score, isUser, null)
        }
        else loadUserInfo(pp, pseudo, score, isUser, Id)

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

        profilePic = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.profilePic)
        pseudoUnderPP = navigationView.getHeaderView(0).findViewById<TextView>(R.id.pseudoUnderPP)
        if (auth.currentUser != null)
            loadUserInfo(profilePic, pseudoUnderPP, null, true, null)

        getQuizz()
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


    fun getQuizz(){
        val quizz_list = mutableListOf<Quizz>()
        val queryRef = if (isUser){
            db.collection("quizz").whereEqualTo("Createur", auth.currentUser!!.uid)
        }
        else db.collection("quizz").whereEqualTo("Createur", Id)

        queryRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("HELLO", "${document.id} => ${document.data}")
                    val quizz = document.toObject(Quizz::class.java)
                    quizz.id = document.id
                    quizz_list.add(quizz)
                    listAdapter.setList(quizz_list.toTypedArray())
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Hello", "Error getting documents: ", exception)
            }
    }
    override fun onBackPressed(){
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View) {
        if (v.scaleX == 1f) {
            v.animate().apply {
                duration = 100
                scaleXBy(.2f)
                scaleYBy(.2f)
            }.withEndAction {
                v.animate().apply {
                    duration = 100
                    scaleXBy(-.2f)
                    scaleYBy(-.2f)
                }
                if (v == card){
                    addFriend(Id)
                }
            }.start()
        }
    }
}