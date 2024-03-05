package com.cyliann.quizzproj

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.squareup.picasso.Picasso

class Friends: BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var listAdapter: FriendsAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profilePic: ImageView
    private lateinit var pseudoUnderPP: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        listAdapter = FriendsAdapter(this, arrayOf(), this)
        val listView = findViewById<ListView>(R.id.listViewFriends)
        listView.adapter = listAdapter

        getFriendsUID()

        supportActionBar?.hide()

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)

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

    }

    private fun getFriendsUID(){
        db.collection("user").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
            if (document != null) {
                // Accédez aux données du document
                val data = document.data
                if (data != null) {
                    val user = document.toObject(User::class.java)
                    user!!.uid = document.id
                    getFriends(user)
                } else {
                    println("Aucune donnée trouvée dans le document")
                }
            } else {
                println("Aucun document trouvé")
            }
        }
    }

    private fun getFriends(user: User){
        var friends_list = mutableListOf<User>()
        for (uid in user.friendsList){
            db.collection("user").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Accédez aux données du document
                        val data = document.data
                        if (data != null) {
                            val friend = document.toObject(User::class.java)
                            friend!!.uid = document.id
                            friends_list.add(friend)
                            listAdapter.setList(friends_list.toTypedArray())

                        } else {
                            println("Aucune donnée trouvée dans le document")
                        }
                    } else {
                        println("Aucun document trouvé")
                    }
                }
        }
    }

    fun showConfirmationDialog(id : String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirmation")
        alertDialogBuilder.setMessage("Êtes-vous sûr de vouloir supprimer cet ami ?")
        alertDialogBuilder.setPositiveButton("Oui") { dialog, which ->
            deleteFriend(id)
        }
        alertDialogBuilder.setNegativeButton("Annuler") { dialog, which ->

        }
        alertDialogBuilder.setOnCancelListener {

        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
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