package com.cyliann.quizzproj

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ChoixQuizzAdapter(private val context: Context, private var quizz: Array<Quizz>) : BaseAdapter(), OnClickListener
{
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
    }

    private fun initHolder(view: View): Holder {
        val holder = Holder()
        holder.title = view.findViewById(R.id.quizz_name)
        holder.img = view.findViewById(R.id.playImg)
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
            cv = inflater!!.inflate(R.layout.list_item_layout, parent, false)
        }
        val holder = initHolder(cv!!)
        holder.title.text = quizz[position].title
        holder.img.setOnClickListener(this)
        holder.img.setTag(quizz[position].id)
        return cv
    }

    override fun onClick(v: View?) {
        if (v!!.scaleX == 1f) {
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
                val i = Intent(context, QuizzGame::class.java)
                i.putExtra("docId", v.tag.toString())
                context.startActivity(i)
            }.start()
        }
    }

}