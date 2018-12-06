package com.android.quinnmc.faceradio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.quinnmc.faceradio.Deejay.URIToMusicMap
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.chat_from_row.view.*

class ProfileActivity: AppCompatActivity() {

    companion object {
        val TAG = "Profile"
        val USER_KEY = "USER_KEY"
    }

    var profUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = profUser?.username

        println("In Profile Activity")
        println("Happy Playlists:")
        println(profUser?.happyPlaylists)
        println("Passive Playlists:")
        println(profUser?.passivePlaylists)
        println("Sleepy Playlists:")
        println(profUser?.sleepyPlaylists)


        val happySongs = arrayListOf<String>()
        val happyURIs = profUser?.happyPlaylists
        for (uri in happyURIs!!) {
            happySongs.add(URIToMusicMap[uri]!!)
        }
        happy_playlists_field.text = happySongs.joinToString()

        val passiveSongs = arrayListOf<String>()
        val passiveURIs = profUser?.passivePlaylists
        for (uri in passiveURIs!!) {
            passiveSongs.add(URIToMusicMap[uri]!!)
        }
        passive_playlists_field.text = passiveSongs.joinToString()

        val sleepySongs = arrayListOf<String>()
        val sleepyURIs = profUser?.sleepyPlaylists
        for (uri in sleepyURIs!!) {
            sleepySongs.add(URIToMusicMap[uri]!!)
        }
        sleepy_playlists_field.text = sleepySongs.joinToString()

        profile_username_label.text = profUser?.username

        val img_uri = profUser?.profileImageUrl
        Picasso.get().load(img_uri).into(profile_image)

        latest_pair_profile.text = profUser?.latestString()

    }

}