package com.cyliann.quizzproj

import android.app.AlertDialog
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
import kotlin.properties.Delegates

class CreatedQuizzList: BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var listAdapter: CreatedQuizzListAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profilePic: ImageView
    private lateinit var pseudoUnderPP: TextView
    private var isUser by Delegates.notNull<Boolean>()
    private lateinit var createurId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createdquizzlist)
        if (!intent.getStringExtra("docId").isNullOrEmpty()){
            createurId = intent.getStringExtra("docId").toString()
            isUser = false
        }

        if ((intent.getBooleanExtra("isUser", false) == true)){
            isUser = true
        }

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)

        listAdapter = CreatedQuizzListAdapter(this, arrayOf(), this, null, auth)

        val listView = findViewById<ListView>(R.id.listViewQuizz)
        listView.adapter = listAdapter
        supportActionBar?.hide()

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

        val buttonCreate = findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCreate)

        buttonCreate.setOnClickListener {
            showCreateQuizzDialog()
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        profilePic = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.profilePic)
        pseudoUnderPP = navigationView.getHeaderView(0).findViewById<TextView>(R.id.pseudoUnderPP)
        if (auth.currentUser != null)
            loadUserInfo(profilePic, pseudoUnderPP, null, true, null)

        getMyQuizz()

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

    fun getMyQuizz(){
        val quizz_list = mutableListOf<Quizz>()
        val queryRef = if (isUser){
            db.collection("quizz").whereEqualTo("Createur", auth.currentUser!!.uid)
        }
        else db.collection("quizz").whereEqualTo("Createur", createurId)

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

    fun showCreateQuizzDialog(){
        Toast.makeText(this, "Pas encore implémenté", Toast.LENGTH_SHORT).show()

    }

    fun showConfirmationDialog(id : String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("Êtes-vous sûr de vouloir supprimer ce Quiz ?")
        alertDialogBuilder.setPositiveButton("Oui") { dialog, which ->
            db.collection("quizz").document(id).delete()
            getMyQuizz()
        }
        alertDialogBuilder.setNegativeButton("Annuler") { dialog, which ->

        }
        alertDialogBuilder.setOnCancelListener {
            
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    override fun onBackPressed(){
        super.onBackPressed()
        finish()
    }
}