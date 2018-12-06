package com.android.quinnmc.faceradio

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {

    var optionalPreset: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select Friend"

        val adapter = GroupAdapter<ViewHolder>()

//        adapter.add(UserItem())
//        adapter.add(UserItem())
//        adapter.add(UserItem())

        recyclerView_newMessage.adapter = adapter

        optionalPreset = intent.getStringExtra(RadioFragment.USER_KEY)

        fetchUsers()
    }

    companion object {
        val USER_KEY = "USER_KEY"
        val MSG_KEY = "MSG_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d("newMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, MessageLogActivity::class.java)
                    //intent.putExtra(USER_KEY, item.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    // if from share
                    intent.putExtra(MSG_KEY, optionalPreset)
                    //
                    startActivity(intent)

                    finish()
                }

                recyclerView_newMessage.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        // Text into row
        viewHolder.itemView.username_textview_new_message.text = user.username
        // Image into row
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageview_new_messge_row)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}

// tedious
//class CustomAdapter : RecyclerView.Adapter
