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
        if (prevFace == null || (counter > 200 && dominantEmotion(face) != dominantEmotion(prevFace!!))) {
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

        if (leftEyeProb < 0.4 && rightEyeProb < 0.4) {
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
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                safeAfterNewFace(face)
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }


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
