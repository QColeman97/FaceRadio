package com.android.quinnmc.faceradio

import android.view.View
import com.android.quinnmc.faceradio.Deejay.ref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.friend_row.view.*
import kotlinx.android.synthetic.main.latest_message_row.view.*


class FriendRow(val user: User): Item<ViewHolder>() {

    var friendUser: User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // Include later
        //viewHolder.itemView.friend_textview_latest_song.text = chatMessage.text
        //viewHolder.itemView.friend_textview_latest_song.visibility = View.INVISIBLE

        viewHolder.itemView.friend_textview_latest_song.text = user.latestString()

        // OLD
        //viewHolder.itemView.message_textview_latest_message.text = "SOMETHING"

        // FOR RECENT MSG AND PROFILE PIC
//        val friendId: String
//        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
//            friendId = chatMessage.toId
//        } else {
//            friendId = chatMessage.fromId
//        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/${user.uuid}") // friendId
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                friendUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_friend.text = friendUser?.username

                val targetImageView = viewHolder.itemView.imageview_friend
                Picasso.get().load(friendUser?.profileImageUrl).into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.friend_row
    }
}