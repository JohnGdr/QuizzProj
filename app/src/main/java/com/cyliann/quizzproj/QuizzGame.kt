package com.cyliann.quizzproj

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FieldValue


class QuizzGame: BaseActivity() {

    lateinit var quizz: Quizz
    lateinit var questionTV: TextView
    lateinit var reponse1: TextView
    lateinit var reponse2: TextView
    lateinit var reponse3: TextView
    lateinit var reponse4: TextView
    lateinit var titleView: TextView
    lateinit var confirm: TextView
    private lateinit var chronometer: Chronometer
    private var quizStartTime: Long = 0
    private var estimatedTime: Int = 0
    private var pointToGain: Int = 0
    var score: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.quizz_game)
        val docId = intent.getStringExtra("docId")
        Log.e("docId", docId.toString())

        db.collection("quizz")
            .document(docId!!).get()
            .addOnSuccessListener { document ->
                quizz = document.toObject(Quizz::class.java)!!
                titleView = findViewById<TextView>(R.id.titleView)
                questionTV = findViewById<TextView>(R.id.question)
                reponse1 = findViewById<TextView>(R.id.reponse1)
                reponse2 = findViewById<TextView>(R.id.reponse2)
                reponse3 = findViewById<TextView>(R.id.reponse3)
                reponse4 = findViewById<TextView>(R.id.reponse4)
                confirm = findViewById<TextView>(R.id.confirm)

                titleView.text = quizz.Titre
                loadQuestion(0)
            }

        supportActionBar?.hide()

        val window: Window = getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = getResources().getColor(R.color.dark_blue)
        chronometer = findViewById(R.id.chronometer)
        quizStartTime = SystemClock.elapsedRealtime()

        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()
    }

    private fun loadQuestion(position: Int){
        if (position > quizz.Questions.entries.size - 1){
            chronometer.stop()
            val elapsedTime = SystemClock.elapsedRealtime() - quizStartTime
            val seconds = (elapsedTime / 1000).toInt()
            if (seconds <= estimatedTime) pointToGain += 1
            if (score != 0){
                if (quizz.Questions.entries.size / score == 1){
                    pointToGain += 2
                }
                else if (quizz.Questions.entries.size / score >= 0.5){
                    pointToGain += 1
                }
            }
            if(auth.currentUser != null){
                val increment = FieldValue.increment(pointToGain.toDouble())
                db.collection("user").document(auth.currentUser!!.uid)
                    .update("totalscore", increment)
                    .addOnSuccessListener {
                        println("incrémenté")
                    }
                    .addOnFailureListener{
                        println("failed incrémenté")
                    }
            }
            val popup = PopupDialog.newInstance(score, seconds, pointToGain, quizz.Questions.entries.size)
            popup.show(supportFragmentManager, "Popup")
            Toast.makeText(this, "Score: " + score.toString() + "/" + quizz.Questions.entries.size+ " en " + seconds + " secondes ! Vous avez obtenu "+ pointToGain +"/3 points !", Toast.LENGTH_SHORT).show()
            return
        }
        val question = quizz.Questions.entries.toTypedArray().get(position)
        reloadUI(question.key, question.value, position)
    }

    private fun reloadUI(question: String, reponses: HashMap<String, Boolean>, position: Int){
        val tabSelected = arrayOf(false, false, false, false)
        questionTV.text = question
        var isCorrect: Boolean? = null
        val shuffledReponses = reponses.entries.shuffled()
        val nombreBonnesReponses = shuffledReponses.count { it.value == true }
        reponse1.text = shuffledReponses.get(0).key
        reponse2.text = shuffledReponses.get(1).key
        reponse3.text = shuffledReponses.get(2).key
        reponse4.text = shuffledReponses.get(3).key
        reponse1.tag = shuffledReponses.get(0).value
        reponse2.tag = shuffledReponses.get(1).value
        reponse3.tag = shuffledReponses.get(2).value
        reponse4.tag = shuffledReponses.get(3).value
        reponse1.setBackgroundColor(resources.getColor(R.color.dark_blue))
        reponse2.setBackgroundColor(resources.getColor(R.color.dark_blue))
        reponse3.setBackgroundColor(resources.getColor(R.color.dark_blue))
        reponse4.setBackgroundColor(resources.getColor(R.color.dark_blue))

        reponse1.setOnClickListener{
            isCorrect = reponse1.tag as Boolean
            if (tabSelected[0] == true){
                reponse1.setBackgroundColor(resources.getColor(R.color.dark_blue))
                tabSelected[0] = false
            }
            else {
                reponse1.setBackgroundColor(resources.getColor(R.color.yellow))
                tabSelected[0] = true
            }

        }
        reponse2.setOnClickListener{
            isCorrect = reponse2.tag as Boolean
            if (tabSelected[1] == true){
                reponse2.setBackgroundColor(resources.getColor(R.color.dark_blue))
                tabSelected[1] = false
            }
            else {
                reponse2.setBackgroundColor(resources.getColor(R.color.yellow))
                tabSelected[1] = true
            }
        }
        reponse3.setOnClickListener{
            isCorrect = reponse3.tag as Boolean
            if (tabSelected[2] == true){
                reponse3.setBackgroundColor(resources.getColor(R.color.dark_blue))
                tabSelected[2] = false
            }
            else {
                reponse3.setBackgroundColor(resources.getColor(R.color.yellow))
                tabSelected[2] = true
            }
        }
        reponse4.setOnClickListener{
            isCorrect = reponse4.tag as Boolean
            if (tabSelected[3] == true){
                reponse4.setBackgroundColor(resources.getColor(R.color.dark_blue))
                tabSelected[3] = false
            }
            else {
                reponse4.setBackgroundColor(resources.getColor(R.color.yellow))
                tabSelected[3] = true
            }
        }

        confirm.setOnClickListener{
            val bonnesReponsesSelectionnees = tabSelected.indices.count {
                tabSelected[it] && shuffledReponses[it].value == true
            }
            val mauvaisesReponsesSelectionnees = tabSelected.indices.count {
                tabSelected[it] && shuffledReponses[it].value == false
            }
            if (isCorrect != null){
                if (bonnesReponsesSelectionnees == nombreBonnesReponses && mauvaisesReponsesSelectionnees == 0) {
                    score += 1
                }
                loadQuestion(position + 1 )
                estimatedTime += 20
            }
        }

    }
}

class PopupDialog : DialogFragment() {

    companion object {
        fun newInstance(score: Int, timer: Int, stars: Int, nbQuestions: Int): PopupDialog {
            val args = Bundle().apply {
                putInt("score", score)
                putInt("timer", timer)
                putInt("stars", stars)
                putInt("nbQuestions", nbQuestions)
            }
            val fragment = PopupDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.popup_layout, container, false)
        val score = arguments?.getInt("score", 0) ?: 0
        val timer = arguments?.getInt("timer", 0) ?: 0
        val stars = arguments?.getInt("stars", 0)?: 0
        val minutes = timer / 60
        val seconds = timer % 60
        val formattedTime = String.format("%02d:%02d", minutes, seconds)
        val nbQuestions = arguments?.getInt("nbQuestions", 0) ?: 0
        view.findViewById<TextView>(R.id.score).text = "Score: " + score + "/" + nbQuestions
        view.findViewById<TextView>(R.id.timer).text = "Timer: $formattedTime"
        if (stars == 1){
            view.findViewById<ImageView>(R.id.star1).setImageResource(R.drawable.baseline_star_rate_yellow)
        }
        else if (stars == 2){
            view.findViewById<ImageView>(R.id.star1).setImageResource(R.drawable.baseline_star_rate_yellow)
            view.findViewById<ImageView>(R.id.star2).setImageResource(R.drawable.baseline_star_rate_yellow)
        }
        else if (stars == 3){
            view.findViewById<ImageView>(R.id.star1).setImageResource(R.drawable.baseline_star_rate_yellow)
            view.findViewById<ImageView>(R.id.star2).setImageResource(R.drawable.baseline_star_rate_yellow)
            view.findViewById<ImageView>(R.id.star3).setImageResource(R.drawable.baseline_star_rate_yellow)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton: Button = view.findViewById(R.id.close_button)

        closeButton.setOnClickListener {
            dismiss()
            activity?.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false
    }
}

