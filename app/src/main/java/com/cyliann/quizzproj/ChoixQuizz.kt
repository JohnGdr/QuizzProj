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
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class ChoixQuizz : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var listAdapter: ChoixQuizzAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var profilePic: ImageView
    private lateinit var pseudoUnderPP: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choix_quizz)

        listAdapter = ChoixQuizzAdapter(this, arrayOf())
        supportActionBar?.hide()

        getQuizz(mutableListOf())

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)


        val listView = findViewById<ListView>(R.id.listViewQuizz)
        listView.adapter = listAdapter

        val buttonRechercher = findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonRechercher)

        buttonRechercher.setOnClickListener {
            showTagSelectionDialog()
        }



        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.z = -1F
        drawerLayout.addDrawerListener(object: DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                drawerLayout.z = 1F
            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.z = -1F
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        val buttonMenu = findViewById<ImageView>(R.id.menu)
        buttonMenu.isClickable = true
        
        buttonMenu.setOnClickListener{
            drawerLayout.z = 1F
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        profilePic = navigationView.getHeaderView(0).findViewById<ImageView>(R.id.profilePic)
        pseudoUnderPP = navigationView.getHeaderView(0).findViewById<TextView>(R.id.pseudoUnderPP)
        if (auth.currentUser != null)
            loadUserInfo(profilePic, pseudoUnderPP, null, true, null)
    }

    private fun showTagSelectionDialog() {
        var checkedTags = BooleanArray(tags.size)
        var checkedTagsName = mutableListOf<String>()

        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("Sélectionner les tags")
            .setMultiChoiceItems(tags.toTypedArray(), checkedTags) { dialog, which, isChecked ->
                if (isChecked){
                    checkedTagsName.add(tags.get(which))
                }
            }
            .setPositiveButton("OK") { dialog, id ->
                getQuizz(checkedTagsName)
            }
            .setNegativeButton("Annuler") { dialog, id ->
            }

        val dialog = builder.create()
        dialog.show()
    }
    fun getQuizz(query: MutableList<String>) {
        val quizz_list = mutableListOf<Quizz>()

        val queryRef = if (!query.isNullOrEmpty()) {
            db.collection("quizz").whereArrayContainsAny("Tags", query)
        } else {
            db.collection("quizz")
        }

        queryRef.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("HELLO", "${document.id} => ${document.data}")
//                    val quizz = Quizz(document.data.get("Titre").toString(),document.id, (document.data.get("Createur")).toString())
                    val quizz = document.toObject(Quizz::class.java)
                    quizz.id = document.id
                    db.collection("user").document(quizz.Createur!!).get().addOnSuccessListener {
                        val u = it.toObject(User::class.java)!!
                        quizz.pseudoCreateur = u.pseudo
                        quizz_list.add(quizz)
                        listAdapter.setList(quizz_list.toTypedArray())
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Hello", "Error getting documents: ", exception)
            }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (auth.currentUser != null)
                FirebaseAuth.getInstance().signOut()
            super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
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
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}