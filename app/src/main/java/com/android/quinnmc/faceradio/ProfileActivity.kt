package com.android.quinnmc.faceradio

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.chat_from_row.view.*

class ProfileActivity: AppCompatActivity() {

    companion object {
        val TAG = "Profile"
    }

    val URIToMusicMap =
        hashMapOf("spotify:user:spotify:playlist:37i9dQZF1DX0Yxoavh5qJV" to "Christmas Hits",
            "spotify:user:spotify:playlist:37i9dQZF1DWSNC7AjZWNry" to "Queen",
            "spotify:user:spotify:playlist:37i9dQZF1DWZAkrucRF6Gq" to "Daft Punk",
            "spotify:user:129768214:playlist:6fol9qIRweKG8M7NbbCHSw" to "Michael Buble",
            "spotify:user:12127647737:playlist:1DVU0Zy0B5MX5B6ZG7ohfO" to "Thrash Metal",
            "spotify:user:spotify:playlist:37i9dQZF1DX1PfYnYcpw8w" to "Ariana Grande",
            "spotify:user:spotify:playlist:37i9dQZF1DX7IOI7TbS1hG" to "Flume",
            "spotify:user:spotify:playlist:37i9dQZF1DX6ziVCJnEm59" to "Coffeehouse Tunes")

    var profUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = profUser?.username

        val happySongs = arrayListOf<String>()
        val happyURIs = profUser?.happyPlaylists
        for (uri in happyURIs!!) {
            happySongs.add(URIToMusicMap[uri]!!)
        }
        happy_playlists_field.text = happySongs.joinToString()

        val passiveSongs = arrayListOf<String>()
        val passiveURIs = profUser?.passivePlaylists
        for (uri in happyURIs!!) {
            passiveSongs.add(URIToMusicMap[uri]!!)
        }
        passive_playlists_field.text = happySongs.joinToString()

        val sleepySongs = arrayListOf<String>()
        val sleepyURIs = profUser?.sleepyPlaylists
        for (uri in happyURIs!!) {
            sleepySongs.add(URIToMusicMap[uri]!!)
        }
        sleepy_playlists_field.text = happySongs.joinToString()

        profile_username_label.text = profUser?.username

        val img_uri = profUser?.profileImageUrl
        Picasso.get().load(img_uri).into(profile_image)

    }

}