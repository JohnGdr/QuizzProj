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
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso

class ClassementAdapter(private val context: Context, private var users:Array<User>) : BaseAdapter(), OnClickListener {
    companion object {
        private var inflater: LayoutInflater? = null
    }

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setList(list: Array<User>){
        users = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class Holder {
        lateinit var name: TextView
        lateinit var pp: ImageView
        lateinit var score: TextView
        lateinit var tropheefirst: ImageView
    }

    private fun initHolder(view: View): Holder {
        val holder = Holder()
        holder.name = view.findViewById(R.id.user_name)
        holder.pp = view.findViewById(R.id.profilePic)
        holder.score = view.findViewById(R.id.score)
        holder.tropheefirst = view.findViewById(R.id.firstImg)
        holder.name.maxLines = 1
        holder.name.isSelected = true
        holder.name.isSingleLine = true
        holder.name.isFocusable = true
        holder.name.isFocusableInTouchMode = true
        return holder
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView
        if (cv == null) {
            cv = inflater!!.inflate(R.layout.classement_layout, parent, false)
        }
        val holder = initHolder(cv!!)
        holder.name.text = users[position].pseudo
        holder.name.setOnClickListener(this)
        holder.name.setTag(users[position].uid)
        if (!users[position].pp.isNullOrEmpty()){
            Picasso.with(context).load(users[position].pp).into(holder.pp)
        }
        if (position == 0){
            holder.tropheefirst.background = ContextCompat.getDrawable(context, R.drawable.trophee)
        }
        holder.score.text = users[position].totalscore.toString()

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
                if (v.id == R.id.user_name){
                    val i = Intent(context, Profil()::class.java)
                    i.putExtra("docId", v.tag.toString())
                    context.startActivity(i)
                }
            }.start()
        }
    }
}