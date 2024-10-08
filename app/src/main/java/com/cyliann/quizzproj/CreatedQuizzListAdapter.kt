package com.cyliann.quizzproj

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.view.View.OnClickListener
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class CreatedQuizzListAdapter(private val context: Context, private var quizz: Array<Quizz>, private val activityQuizzList: CreatedQuizzList?, private val activityProfil: Profil?, private val auth: FirebaseAuth): BaseAdapter(), OnClickListener {
    companion object {
        private var inflater: LayoutInflater? = null
    }



    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setList(list: Array<Quizz>){
        quizz = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return quizz.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class Holder {
        lateinit var title: TextView
        lateinit var img: ImageView
        lateinit var createur: TextView
        lateinit var modifyImg: ImageView
        lateinit var deleteImg: ImageView
    }

    private fun initHolder(view: View): Holder {
        val holder = Holder()
        holder.title = view.findViewById(R.id.quizz_name)
        holder.img = view.findViewById(R.id.playImg)
        holder.modifyImg = view.findViewById(R.id.modifyImg)
        holder.deleteImg = view.findViewById(R.id.deleteImg)
        holder.title.maxLines = 1
        holder.title.isSelected = true
        holder.title.isSingleLine = true
        holder.title.isFocusable = true
        holder.title.isFocusableInTouchMode = true
        return holder
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView
        if (cv == null) {
            cv = inflater!!.inflate(R.layout.created_quizz_layout, parent, false)
        }
        val holder = initHolder(cv!!)
        holder.title.text = quizz[position].Titre
        holder.img.setOnClickListener(this)
        holder.img.setTag(quizz[position].id)
        if (activityQuizzList != null){
            holder.modifyImg.setOnClickListener(this)
            holder.modifyImg.setTag(quizz[position].id)
            holder.deleteImg.setOnClickListener(this)
            holder.deleteImg.setTag(quizz[position].id)
        }
        else if ((activityProfil != null) && (quizz[position].Createur != auth.currentUser!!.uid) ){
            holder.modifyImg.visibility = View.INVISIBLE
            holder.modifyImg.isEnabled = false
            holder.deleteImg.visibility = View.INVISIBLE
            holder.deleteImg.isEnabled = false
        }
        return cv
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
                if (v.id == R.id.playImg){
                    val i = Intent(context, QuizzGame::class.java)
                    i.putExtra("docId", v.tag.toString())
                    context.startActivity(i)
                }
                else if (v.id == R.id.modifyImg){
                    Toast.makeText(context, "modifier", Toast.LENGTH_SHORT).show()
                }
                else if (v.id == R.id.deleteImg){
                    Toast.makeText(context, "supprimer", Toast.LENGTH_SHORT).show()
                    val i = v.tag.toString()
                    activityQuizzList!!.showConfirmationDialog(i)
                }
            }.start()
        }
    }

}