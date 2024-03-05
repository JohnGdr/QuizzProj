package com.cyliann.quizzproj

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import com.google.firebase.firestore.FieldValue


open class BaseActivity : AppCompatActivity() {

    val TAG = "BaseActivity.class"

    val db = Firebase.firestore
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val storage: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var capturedImage: Bitmap
    private lateinit var chosenImageTextField : TextView
    lateinit var tags: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db.collection("variables").document("Tags").get()
            .addOnSuccessListener { document ->
                val data = document.data
                tags = data!!["TagList"] as List<String>
            }
        }

    fun registerUser(pseudo: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    writeNewUser(pseudo, email)
                    // Registration successful
                } else {
                    // Registration failed
                    Log.w("Registration", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Erreur, pensez à vérifier l'adresse mail ou le mot de passe", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun loadUserInfo(profilePic: ImageView, pseudoUnderPP: TextView, score: TextView?, useCurrentUser: Boolean, Id: String?) {
        val documentReference = if (useCurrentUser) {
            db.collection("user").document(auth.currentUser!!.uid)
        } else {
            db.collection("user").document(Id!!) // Remplacer "createurId" par la variable appropriée
        }

        documentReference.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Accédez aux données du document
                    val data = document.data
                    if (data != null) {
                        val u = document.toObject(User::class.java)!!

                        pseudoUnderPP.text = u.pseudo
                        if (!(u.pp.isNullOrEmpty()) && u.pp.toString() != "null"){
                            Picasso.with(this).load(u.pp).into(profilePic)
                        }
                        if (score != null)
                            score.text = u.totalscore.toString()
                    } else {
                        println("Aucune donnée trouvée dans le document")
                    }
                } else {
                    println("Aucun document trouvé")
                }
            }
    }


    fun writeNewUser(pseudo: String, email: String) {
        val user = User(pseudo, email)
        db.collection("user").document(auth.currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                finish()
                Log.d("writeNewUser", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e -> Log.w("writeNewUser", "Error writing document", e) }
    }

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    finish()
                    startActivity(Intent(this, ChoixQuizz::class.java))
                } else {
                    // Login failed
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Identifiants incorrect", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun showChangePPDialog(profilePic: ImageView, pseudoUnderPP: TextView) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_alert_dialog, null)
        dialogBuilder.setView(dialogView)

        chosenImageTextField = dialogView.findViewById<TextView>(R.id.textViewMessage)

        val buttonPhoto = dialogView.findViewById<Button>(R.id.buttonPhoto)
        val buttonUpload = dialogView.findViewById<Button>(R.id.buttonUpload)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        buttonPhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        buttonUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
        buttonConfirm.setOnClickListener{
            if (::capturedImage.isInitialized) {
                uploadImageToFirebaseStorage(capturedImage, profilePic, pseudoUnderPP)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    capturedImage = imageBitmap
                }

                REQUEST_IMAGE_PICK -> {
                    val selectedImage = data?.data
                    val imageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                    val rotatedBitmap = rotateBitmap(selectedImage!!, imageBitmap)
                    capturedImage = rotatedBitmap
                }
            }
            chosenImageTextField.text = "Image choisie"
        }
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap, profilePic: ImageView, pseudoUnderPP: TextView) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // L'image a été téléchargée avec succès, maintenant obtenir son URL de téléchargement
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageURL = uri.toString()

                // Enregistrer l'URL de téléchargement dans Firestore
                saveImageURLToFirestore(imageURL, profilePic, pseudoUnderPP)
            }
        }.addOnFailureListener { exception ->
            // Gérer les échecs de téléchargement de l'image
            Log.e(TAG, "Failed to upload image to Firebase Storage: $exception")
        }
    }

    private fun saveImageURLToFirestore(imageURL: String, profilePic: ImageView, pseudoUnderPP: TextView) {
        val db = FirebaseFirestore.getInstance()
        val imageRef = db.collection("user").document(auth.currentUser!!.uid)


        imageRef.update("pp", imageURL)
            .addOnSuccessListener {
                // L'URL de l'image a été enregistrée avec succès dans Firestore
                Log.d(TAG, "Image URL saved to Firestore")
                loadUserInfo(profilePic, pseudoUnderPP, null, true, null)
            }
            .addOnFailureListener { exception ->
                // Gérer les échecs de sauvegarde de l'URL de l'image dans Firestore
                Log.e(TAG, "Failed to save image URL to Firestore: $exception")
            }
    }

    fun getOrientation(path: String): Int {
        var orientation = 0
        try {
            val exif = ExifInterface(path)
            orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return orientation
    }

    // Cette fonction réoriente l'image en fonction de l'orientation spécifiée
    fun rotateBitmap(bitmapPath: Uri, bitmap: Bitmap): Bitmap {
        val picturePath = uriToFile(bitmapPath)
        val orientation = getOrientation(picturePath)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270F)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun uriToFile(uri: Uri): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()

        val columnIndex = cursor!!.getColumnIndex(filePathColumn[0])
        val picturePath = cursor!!.getString(columnIndex)
        cursor!!.close()
        return picturePath
    }

    fun addFriend(uid: String){
        val userRef = db.collection("user").document(auth.currentUser!!.uid)

        userRef.update("friendsList", FieldValue.arrayUnion(uid))
            .addOnSuccessListener {
                Log.d(TAG, "Ami ajouté avec succès")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erreur lors de l'ajout d'un ami", e)
            }
    }

    fun deleteFriend(uid: String) {
        val userRef = db.collection("user").document(auth.currentUser!!.uid)

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val userData = documentSnapshot.toObject(User::class.java)

                userData?.let { user ->
                    val updatedFriendsList = user.friendsList.filter { friendUid ->
                        friendUid != uid
                    }

                    userRef.update("friendsList", updatedFriendsList)
                        .addOnSuccessListener {
                            println("Ami supprimé avec succès.")
                        }
                        .addOnFailureListener { exception ->
                            println("Erreur lors de la suppression de l'ami : $exception")
                        }
                }
            } else {
                println("Le document utilisateur n'existe pas.")
            }
        }.addOnFailureListener { exception ->
            println("Erreur lors de la récupération du document utilisateur : $exception")
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}