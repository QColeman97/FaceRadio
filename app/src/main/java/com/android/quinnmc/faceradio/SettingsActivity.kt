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
import com.android.quinnmc.faceradio.Deejay.musicToURIMap
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
        val TAG = "SettingsActivity"
        val HAPPY = 0
        val PASSIVE = 2
        val SLEEPY = 3
    }

    val happySelections = ArrayList<String>()
    val passiveSelections = ArrayList<String>()
    val sleepySelections = ArrayList<String>()

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
        }
    }

    private fun updateUserPlaylists() {
        val happyURIs = playlistURIs(happySelections)
        val passiveURIs = playlistURIs(passiveSelections)
        val sleepyURIs = playlistURIs(sleepySelections)

        if (currentUser != null) {
            val my_id = currentUser!!.uuid
            val ref = FirebaseDatabase.getInstance().getReference("/users/$my_id")
            val new_user = User(currentUser!!.uuid, currentUser!!.username, currentUser!!.profileImageUrl,
                happyURIs, passiveURIs, sleepyURIs, currentUser!!.latestEmotion, currentUser!!.latestSong)
            ref.setValue(new_user).addOnSuccessListener {
                val intent = Intent(this, RadioActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
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
                            happySelections.add(arrayPlaylists[i])
                            // new
                            arrayChecked[i] = false
                        }
                    }
                    happy_selection.text = happySelections.joinToString()
                }
                PASSIVE -> {
                    for (i in 0 until arrayPlaylists.size) {
                        if (arrayChecked[i]) {
                            passiveSelections.add(arrayPlaylists[i])
                            arrayChecked[i] = false
                        }
                    }
                    passive_selection.text = passiveSelections.joinToString()
                }
                SLEEPY -> {
                    for (i in 0 until arrayPlaylists.size) {
                        if (arrayChecked[i]) {
                            sleepySelections.add(arrayPlaylists[i])
                            arrayChecked[i] = false
                        }
                    }
                    sleepy_selection.text = sleepySelections.joinToString()
                }
            }
        }
        builder.setNeutralButton("CLEAR") { dialog, which ->
            for (i in 0 until arrayChecked.size) {
                arrayChecked[i] = false
                when (tag) {
                    HAPPY -> happySelections.clear()
                    PASSIVE -> passiveSelections.clear()
                    SLEEPY -> sleepySelections.clear()
                }
            }
        }
        builder.setNegativeButton("DISMISS",
            DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
        //builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()

        println("Happy songs:")
        println(happySelections)
        println("Passive songs:")
        println(passiveSelections)
        println("Sleepy songs:")
        println(sleepySelections)
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
