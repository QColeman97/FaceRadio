package com.android.quinnmc.faceradio

import android.content.Context
import android.database.sqlite.SQLiteDatabaseLockedException
import android.provider.MediaStore
import android.util.Log
import com.android.quinnmc.faceradio.SettingsActivity.Companion.HAPPY
import com.android.quinnmc.faceradio.SettingsActivity.Companion.PASSIVE
import com.android.quinnmc.faceradio.SettingsActivity.Companion.SLEEPY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kotlin.random.Random

object Deejay: FaceDetectionProcessor.FaceDetectionListener {

    private const val HAPPY = "h"
    private const val PASSIVE = "p"
    private const val SLEEPY = "s"
    var currentUser: User? = null
    var prevFace: FirebaseVisionFace? = null
    var counter = 0
    private const val WAIT = 120

    // Fore firebase, fetching uri's and setting latest
    val uid = FirebaseAuth.getInstance().uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

    //constructor() : this(arrayListOf<User>())

//    init {
//        fetchCurrentUser()
//    }

    interface DeejayListener {
        fun onNewPlaylist(uri: Array<String>)
    }

    override fun onNewFace(face: FirebaseVisionFace) {
        if (currentUser == null) {
            fetchCurrentUser(face)
        } else {
            safeAfterNewFace(face)
        }
    }

    fun safeAfterNewFace(face: FirebaseVisionFace) {
        //println("COUNTER: ${counter}")
        if (RadioActivity.mSpotifyAppRemote == null) return

        if (prevFace == null || (counter > WAIT && dominantEmotion(face) != dominantEmotion(prevFace!!))) {
            var rand = Random
            var uri = ""
            var rand_index = 0
            when (dominantEmotion(face)) {
                HAPPY -> {
                    //rand_index = rand(0, currentUser!!.happyPlaylists.size)
                    if (currentUser!!.happyPlaylists.size > 0) {
                        println("SIZE:")
                        println(currentUser!!.happyPlaylists.size)
                        rand_index = rand.nextInt(currentUser!!.happyPlaylists.size)

                        uri = currentUser!!.happyPlaylists[rand_index]

//                        // update user's latest here
//                        val new_user = User(
//                            SettingsActivity.currentUser!!.uuid, SettingsActivity.currentUser!!.username, SettingsActivity.currentUser!!.profileImageUrl,
//                            currentUser!!.happyPlaylists, currentUser!!.passivePlaylists, currentUser!!.sleepyPlaylists, "happy", "")


                        (RadioActivity as DeejayListener).onNewPlaylist(arrayOf(uri, HAPPY))
                    }
                }
                PASSIVE -> {
                    if (currentUser!!.passivePlaylists.size > 0) {
                        println("SIZE:")
                        println(currentUser!!.passivePlaylists.size)
                        rand_index = rand.nextInt(currentUser!!.passivePlaylists.size)
                        uri = currentUser!!.passivePlaylists[rand_index]
                        (RadioActivity as DeejayListener).onNewPlaylist(arrayOf(uri, PASSIVE))
                    }
                }
                SLEEPY -> {
                    if (currentUser!!.sleepyPlaylists.size > 0) {
                        println("SIZE:")
                        println(currentUser!!.sleepyPlaylists.size)
                        rand_index = rand.nextInt(currentUser!!.sleepyPlaylists.size)
                        uri = currentUser!!.sleepyPlaylists[rand_index]
                        (RadioActivity as DeejayListener).onNewPlaylist(arrayOf(uri, SLEEPY))
                    }
                }
            }
            counter = 0
            prevFace = face
        }
        counter += 1
    }

    // from(inclusive) and to(exclusive)
//    fun rand(from: Int, to: Int) : Int {
//        val random = Random
//        return random.nextInt(to - from) + from
//    }

    fun dominantEmotion(face: FirebaseVisionFace): String {
        val leftEyeProb = face.leftEyeOpenProbability
        val rightEyeProb = face.rightEyeOpenProbability
        val smileProb = face.smilingProbability

        if (leftEyeProb < 0.3 && rightEyeProb < 0.3) {
            return SLEEPY
        } else if (smileProb > 0.5) {
            return HAPPY
        } else {
            return PASSIVE
        }
    }

//    fun updateMusic(face: FirebaseVisionFace) {
//        println("INN THE DEEEJJJAAAYYY")
//        //RadioActivity.test
//    }

    private fun fetchCurrentUser(face: FirebaseVisionFace) {
//        val uid = FirebaseAuth.getInstance().uid
//        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                safeAfterNewFace(face)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }


    val URIToMusicMap =
        hashMapOf("spotify:user:spotify:playlist:37i9dQZF1DX0Yxoavh5qJV" to "Christmas Hits",
            "spotify:user:spotify:playlist:37i9dQZF1DWSNC7AjZWNry" to "Queen",
            "spotify:user:spotify:playlist:37i9dQZF1DWZAkrucRF6Gq" to "Daft Punk",
            "spotify:user:spotify:playlist:37i9dQZF1DX5kVmFQxhnLW" to "Michael Buble",
            "spotify:user:spotify:playlist:37i9dQZF1DWWOaP4H0w5b0" to "Thrash Metal",
            "spotify:user:spotify:playlist:37i9dQZF1DX1PfYnYcpw8w" to "Ariana Grande",
            "spotify:user:spotify:playlist:37i9dQZF1DX7IOI7TbS1hG" to "Flume",
            "spotify:user:spotify:playlist:37i9dQZF1DX6ziVCJnEm59" to "Coffeehouse Tunes")

    val musicToURIMap =
        hashMapOf("Christmas Hits"      to "spotify:user:spotify:playlist:37i9dQZF1DX0Yxoavh5qJV",
                  "Queen"               to "spotify:user:spotify:playlist:37i9dQZF1DWSNC7AjZWNry",
                  "Daft Punk"           to "spotify:user:spotify:playlist:37i9dQZF1DWZAkrucRF6Gq",
                  "Michael Buble"       to "spotify:user:spotify:playlist:37i9dQZF1DX5kVmFQxhnLW",
                  "Thrash Metal"        to "spotify:user:spotify:playlist:37i9dQZF1DWWOaP4H0w5b0",
                  "Ariana Grande"       to "spotify:user:spotify:playlist:37i9dQZF1DX1PfYnYcpw8w",
                  "Flume"               to "spotify:user:spotify:playlist:37i9dQZF1DX7IOI7TbS1hG",
                  "Coffeehouse Tunes"   to "spotify:user:spotify:playlist:37i9dQZF1DX6ziVCJnEm59")

//    companion object {
//        private const val HAPPY = 0
//        private const val PASSIVE = 1
//        private const val SLEEPY = 2
//
//        val currentUser: User? = null
//    }
}

//object Deejay {
//
//    var prevFace: FirebaseVisionFace? = null
//
//    var emotion = 0
//
//    fun updateMusic(face: FirebaseVisionFace) {
//        println("INN THE DEEEJJJAAAYYY")
//    }
//
//    fun updateRadio(callback: (Int) -> Unit) {
//        callback(emotion)
//    }
//}
