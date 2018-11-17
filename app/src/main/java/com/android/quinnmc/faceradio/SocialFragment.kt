package com.android.quinnmc.faceradio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_social.*

class SocialFragment : Fragment() {

    private lateinit var listView: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)

        val dummy_strings = listOf("Bob", "Susan")
        val listItems = arrayOfNulls<String>(dummy_strings.size)
        for (i in 0 until dummy_strings.size) {
            listItems[i] = dummy_strings[i]
        }
//        val msgsView = findViewById<ListView>(R.id.message_list)

        //val friendCustomAdapter = FriendCustomAdapter(this)
        //val messageCustomAdapter = MessageCustomAdapter(this)
        //val friendsAdapter = ArrayAdapter(activity, android.R.layout.simple_entry, listItems)
        //val messagesAdapter = ArrayAdapter(activity, simple_entry, listItems)


        val view = inflater.inflate(R.layout.fragment_social, container, false)
//
//        friend_list.setOnItemClickListener(AdapterView.OnItemClickListener() {
//            override fun onItemClick(<Adapter>) {
//
//            }
//        })

        return view
    }

    override fun onStart() {
        super.onStart()
        friendSelect.setOnClickListener {
            toProfile()
        }
        msgSelect.setOnClickListener {
            toMessages()
        }
        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
    }

    private fun toProfile() {
        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
        val bobIntent = Intent(activity, ProfileActivity::class.java)
        startActivity(bobIntent)
    }

    private fun toMessages() {
        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
        val bobIntent = Intent(activity, MessageActivity::class.java)
        startActivity(bobIntent)
    }

//    private class FriendCustomAdapter(context: Context) : BaseAdapter() {
//
//        private val mContext: Context
//
//        init {
//            this.mContext = context
//        }
//
//        override fun getCount(): Int {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return 5
//        }
//
//        override fun getItem(position: Int): Any {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return "friend"
//        }
//
//        override fun getItemId(position: Int): Long {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return position.toLong()
//        }
//
//        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            val textView = TextView(mContext)
//            textView.text = "ROW"
//            return textView
//        }
//    }
//
//    private class MessageCustomAdapter(context: Context) : BaseAdapter() {
//
//        private val mContext: Context
//
//        init {
//            this.mContext = context
//        }
//
//        override fun getCount(): Int {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return 5
//        }
//
//        override fun getItem(position: Int): Any {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return "message"
//        }
//
//        override fun getItemId(position: Int): Long {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            return position.toLong()
//        }
//
//        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            val textView = TextView(mContext)
//            textView.text = "ROW"
//            return textView
//        }
//    }
}