package com.android.quinnmc.faceradio

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.text.method.TextKeyListener.clear
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "SettingsActivity"
        val HAPPY = 0
        val PASSIVE = 2
        val SLEEPY = 3
    }

    val happySongs = ArrayList<String>()
    val passiveSongs = ArrayList<String>()
    val sleepySongs = ArrayList<String>()

    val musicToURIMap =
        hashMapOf("Christmas Hits" to "spotify:user:spotify:playlist:37i9dQZF1DX0Yxoavh5qJV",
            "Queen" to "spotify:user:spotify:playlist:37i9dQZF1DWSNC7AjZWNry",
            "Daft Punk" to "spotify:user:spotify:playlist:37i9dQZF1DWZAkrucRF6Gq",
            "Michael Buble" to "spotify:user:129768214:playlist:6fol9qIRweKG8M7NbbCHSw",
            "Thrash Metal" to "spotify:user:12127647737:playlist:1DVU0Zy0B5MX5B6ZG7ohfO",
            "Ariana Grande" to "spotify:user:spotify:playlist:37i9dQZF1DX1PfYnYcpw8w",
            "Flume" to "spotify:user:spotify:playlist:37i9dQZF1DX7IOI7TbS1hG",
            "Coffeehouse Tunes" to "spotify:user:spotify:playlist:37i9dQZF1DX6ziVCJnEm59")

    val arrayPlaylists = arrayOf("Christmas Hits", "Queen", "Daft Punk", "Michael Buble",
        "Thrash Metal", "Ariana Grande", "Flume", "Coffeehouse Tunes")
    val arrayChecked = booleanArrayOf(false,false,false,false,false,false,false,false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fetchCurrentUser()

        happy_button_settings.setOnClickListener {
            showPlaylistsDialog(HAPPY)
//            happy_selection.text = happySongs.joinToString()
        }
        passive_button_settings.setOnClickListener {
            showPlaylistsDialog(PASSIVE)
//            passive_selection.text = passiveSongs.joinToString()
        }
        sleepy_button_settings.setOnClickListener {
            showPlaylistsDialog(SLEEPY)
//            sleepy_selection.text = sleepySongs.joinToString()
        }
        continue_button_settings.setOnClickListener {

            updateUserPlaylists()

            val intent = Intent(this, RadioActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun updateUserPlaylists() {
        val happyURIs = playlistURIs(happySongs)
        val passiveURIs = playlistURIs(passiveSongs)
        val sleepyURIs = playlistURIs(sleepySongs)

        if (currentUser != null) {
            val my_id = currentUser!!.uuid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$my_id")
            val new_user = User(currentUser!!.uuid, currentUser!!.username, currentUser!!.profileImageUrl,
                happyURIs, passiveURIs, sleepyURIs)
            ref.setValue(new_user)
        }

    }

    private fun playlistURIs(playlist: ArrayList<String>): ArrayList<String> {
        val output = ArrayList<String>()
        for (song in playlist) {
            output.add(musicToURIMap[song]!!)
        }
        return output
    }

    private fun showPlaylistsDialog(tag: Int) {
        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(this)
        when (tag) {
            HAPPY -> builder.setTitle("Happy Songs:")
            PASSIVE -> builder.setTitle("Passive Songs:")
            SLEEPY -> builder.setTitle("Sleepy Songs:")
        }
        builder.setMultiChoiceItems(arrayPlaylists, arrayChecked, {dialog, which, isChecked ->
            arrayChecked[which] = isChecked
            //val playlist = arrayPlaylists[which]
        })
        builder.setPositiveButton("OK") { dialog, which ->
            when (tag) {
                HAPPY -> {
                    for (i in 0 until arrayPlaylists.size) {
                        if (arrayChecked[i]) {
                            happySongs.add(arrayPlaylists[i])
                            // new
                            arrayChecked[i] = false
                        }
                    }
                    happy_selection.text = happySongs.joinToString()
                }
                PASSIVE -> {
                    for (i in 0 until arrayPlaylists.size) {
                        if (arrayChecked[i]) {
                            passiveSongs.add(arrayPlaylists[i])
                            arrayChecked[i] = false
                        }
                    }
                    passive_selection.text = passiveSongs.joinToString()
                }
                SLEEPY -> {
                    for (i in 0 until arrayPlaylists.size) {
                        if (arrayChecked[i]) {
                            sleepySongs.add(arrayPlaylists[i])
                            arrayChecked[i] = false
                        }
                    }
                    sleepy_selection.text = sleepySongs.joinToString()
                }
            }
        }
        builder.setNeutralButton("CLEAR") { dialog, which ->
            for (i in 0 until arrayChecked.size) {
                arrayChecked[i] = false
                when (tag) {
                    HAPPY -> happySongs.clear()
                    PASSIVE -> passiveSongs.clear()
                    SLEEPY -> sleepySongs.clear()
                }
            }
        }
        builder.setNegativeButton("DISMISS",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        //builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()

        println("Happy songs:")
        println(happySongs)
        println("Passive songs:")
        println(passiveSongs)
        println("Sleepy songs:")
        println(sleepySongs)
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d(TAG, "Current user: ${RadioActivity.currentUser?.profileImageUrl}")
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}
