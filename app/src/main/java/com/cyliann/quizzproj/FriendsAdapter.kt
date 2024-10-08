package com.cyliann.quizzproj

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso

class FriendsAdapter(private val context: Context, private var friends: Array<User>, private var activity: Friends) : BaseAdapter(), View.OnClickListener {
    companion object {
        private var inflater: LayoutInflater? = null
    }

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setList(list: Array<User>) {
        friends = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return friends.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class Holder {
        lateinit var pseudo: TextView
        lateinit var img: ImageView
        lateinit var pp: ImageView
    }

    private fun initHolder(view: View): Holder {
        val holder = Holder()
        holder.pseudo = view.findViewById(R.id.friend_name)
        holder.img = view.findViewById(R.id.DeleteImg)
        holder.pp = view.findViewById(R.id.profilePic)
        holder.pseudo.maxLines = 1
        holder.pseudo.isSelected = true
        holder.pseudo.isSingleLine = true
        holder.pseudo.isFocusable = true
        holder.pseudo.isFocusableInTouchMode = true
        return holder
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var cv = convertView
        if (cv == null) {
            cv = inflater!!.inflate(R.layout.list_item_layout_friends, parent, false)
        }
        val holder = initHolder(cv!!)
        holder.pseudo.text = friends[position].pseudo
        holder.pseudo.setOnClickListener(this)
        holder.pseudo.setTag(friends[position].uid)
        if (!friends[position].pp.isNullOrEmpty()){
            Picasso.with(context).load(friends[position].pp).into(holder.pp)
        }
        holder.img.setTag(friends[position].uid)
        holder.img.setOnClickListener(this)
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
                if (v.id == R.id.friend_name){
                    val i = Intent(context, Profil()::class.java)
                    i.putExtra("docId", v.tag.toString())
                    context.startActivity(i)
                }
                else if (v.id == R.id.DeleteImg){
                    activity.showConfirmationDialog(v.tag.toString())
                }
            }.start()
        }
    }

}