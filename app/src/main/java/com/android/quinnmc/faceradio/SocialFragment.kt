package com.android.quinnmc.faceradio

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.fragment_social.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class SocialFragment : Fragment() {

    companion object {
        val TAG = "SocialFragment"
        // Friends
        val USER_KEY = "USER_KEY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_social, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerview_latest_messages.adapter = msg_adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(activity,
            DividerItemDecoration.VERTICAL))

        // Use newmsgactivity method for friends
        recyclerview_friends.adapter = fnds_adapter
        recyclerview_friends.addItemDecoration(DividerItemDecoration(activity,
            DividerItemDecoration.VERTICAL))

        msg_adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "123")
            val intent = Intent(activity, MessageLogActivity::class.java)

            // Safe casting - only type of row in our table
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchUsers()
    }

    val latestMessagesMap = HashMap<String, Message>()

    private fun refreshRecyclerViewMessages() {
        msg_adapter.clear()
        // Add all new messages back
        latestMessagesMap.values.forEach {
            msg_adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {

            // Key is the firebase user id, so hashmap constantly being refreshed between users
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return

                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()

                // OLD
                //adapter.add(LatestMessageRow())
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(Message::class.java) ?: return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()

                // OLD
                //adapter.add(LatestMessageRow())
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
        })
    }

    val msg_adapter = GroupAdapter<ViewHolder>()
    val fnds_adapter = GroupAdapter<ViewHolder>()

//    private fun setupDummyRows() {
//
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//
//    }

    override fun onStart() {
        super.onStart()
        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
        new_msg_button.setOnClickListener {
            toNewMessage()
        }
    }

    // FOR PROFILE
//    private fun toProfile() {
//        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
//        val bobIntent = Intent(activity, ProfileActivity::class.java)
//        startActivity(bobIntent)
//    }
//

    private fun toNewMessage() {
        //Log.d("SOCIAL FRAGMENT", "MAKING INTENT TO PROFILE")
        val newMsgIntent = Intent(activity, NewMessageActivity::class.java)
        startActivity(newMsgIntent)
    }

    // Friends table
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    //Log.d("newMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ProfileActivity::class.java)
                    //intent.putExtra(USER_KEY, item.user.username)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    //finish()
                }

                recyclerview_friends.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}