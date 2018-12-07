package com.android.quinnmc.faceradio

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_radio.*

import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
//import jdk.nashorn.internal.runtime.ECMAException.getException
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import android.content.Intent
import android.graphics.Bitmap
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.view.Menu
import android.view.MenuItem
import com.android.quinnmc.faceradio.Deejay.URIToMusicMap
import com.android.quinnmc.faceradio.Deejay.ref
import com.android.quinnmc.faceradio.R.id.fireFaceOverlay
import com.android.quinnmc.faceradio.R.id.firePreview
import com.android.quinnmc.faceradio.SettingsActivity.Companion.currentUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import kotlinx.android.synthetic.main.fragment_radio.*
import kotlinx.android.synthetic.main.fragment_radio.view.*


/************************************
 *  FACE RADIO RUNNING INFO:
 *  Setup before-hand to use this app successfully,
 *  1) Have Spotify installed on the running device, with a Spotify account created AND signed in on
 *          -- having a non-Premium account works fine, if you don't already have one
 *  2) Please run in the emulator, and configure your AVD camera's front and back to be Webcam0 (laptop's camera)
 *
 *  From profile and msg activites, can get back with the system back-button
 * **********************************/


class RadioActivity() : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback,
        RadioFragment.RadioFragmentListener {

    // Firebase user vars
    val uid = FirebaseAuth.getInstance().uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

    // ML Kit vars
    private var cameraSource: CameraSource? = null
    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                selectedFragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist,
                    currEmotion)

                supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, selectedFragment).commit()

            }
            R.id.navigation_dashboard -> {
                selectedFragment = SocialFragment()
                supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, selectedFragment).commit()
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radio)

        fetchCurrentUser()
        verifyUserIsLoggedIn()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            getRuntimePermissions()
        }

        // TODO: Remove the face graphic, b/c can get data w/o the visual
        // To remove the camera stream feedback
        firePreview.visibility = View.INVISIBLE

//        supportFragmentManager.beginTransaction().
//            replace(R.id.fragment_container, selectedFragment!!).commit()
    }

    private fun fetchCurrentUser() {
//        val uid = FirebaseAuth.getInstance().uid
//        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d(TAG, "Current user: ${currentUser?.profileImageUrl}")
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            cameraSource?.release()
            SpotifyAppRemote.disconnect(mSpotifyAppRemote)

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.upper_nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // FIX - crashes and have to create new account
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPlayPause() {
        if (mSpotifyAppRemote == null) return
        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback {
            if (it.isPaused) {
                selectedFragment.play_pause_btn.setBackgroundResource(android.R.drawable.ic_media_pause)
                mSpotifyAppRemote!!.playerApi.resume()
            } else {
                selectedFragment.play_pause_btn.setBackgroundResource(android.R.drawable.ic_media_play)
                mSpotifyAppRemote!!.playerApi.pause()
            }
        }
    }

    override fun onSkipNext() {
        if (mSpotifyAppRemote == null) return
        mSpotifyAppRemote!!.playerApi.skipNext()
    }

    override fun onSkipPrev() {
        if (mSpotifyAppRemote == null) return
        mSpotifyAppRemote!!.playerApi.skipPrevious()
    }


    /**************************************************************
    ************    SPOTIFY CODE STARTING HERE    *****************
    ***************************************************************/

    override fun onStart() {
        super.onStart()

        selectedFragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist,
            currEmotion)
        supportFragmentManager.beginTransaction().
            replace(R.id.fragment_container, selectedFragment!!).commit()

        val connectionParams =
            ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object: Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d("Spotify", "Connected! Yay!")

                    // First interaction w/ app remote
                    mSpotifyAppRemote!!.getPlayerApi().setShuffle(true)

                    // Subscribe to PlayerState
                    mSpotifyAppRemote!!.getPlayerApi()
                        .subscribeToPlayerState()
                        .setEventCallback(object: Subscription.EventCallback<PlayerState> {

                            override fun onEvent(playerState: PlayerState) {
                                val track = playerState.track
                                if (track != null) {
                                    Log.d("SPOTIFY", "Current track: " + track.name + " by " + track.artist.name);
                                    // Set the text here
                                    currSong = track.name
                                    currArtist = track.artist.name
                                    // Might be a useless call down below
                                    //currAlbumCoverURI = track.imageUri.toString()

                                    // For song progress
//                                    selectedFragment.song_progress.max = track.duration.toInt()
//                                    runOnUiThread({
//                                        var i = 0
//                                        fun run() {
//                                            i += 1
//                                            selectedFragment.song_progress.progress = i
//                                        }
//                                    })

                                    mSpotifyAppRemote!!.imagesApi.getImage(track.imageUri)
                                        .setResultCallback {
                                            if (selectedFragment is RadioFragment) {
                                                selectedFragment.album_cover.setImageBitmap(it)
                                            }
                                        }


                                    // update user's latest here
                                    val latest_user = User(currentUser!!.uuid, currentUser!!.username,
                                        currentUser!!.profileImageUrl, currentUser!!.happyPlaylists,
                                        currentUser!!.passivePlaylists, currentUser!!.sleepyPlaylists,
                                        CURR_EMOTION, track.name + " by " + track.artist.name)
                                    ref.setValue(latest_user).addOnSuccessListener {
                                        Log.d("SPOTIFY", "Updated firebase user's latest!");
                                    }


                                    // Block could be deleted later?
                                    if (selectedFragment is RadioFragment) {
                                        println("SELECTED FRAG IS RADIO " + track.name + " " + track.artist.name)

                                        (selectedFragment as RadioFragment).currEmotion = currEmotion
                                        (selectedFragment as RadioFragment).currSong = currSong
                                        (selectedFragment as RadioFragment).currArtist = currArtist
                                        (selectedFragment as RadioFragment).currFaceId = currFaceGraphic

                                        selectedFragment.current_track_label.text = currSong
                                        selectedFragment.current_artist_label.text = currArtist
//                                        track.album
//                                        track.imageUri
//                                        track.duration
                                    }
                                }
                            }
                        })
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("Spotify", throwable.message, throwable)
                    //Log.e("ERROR", "ERROR connecting to Spotify")
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })

//        if (selectedFragment is RadioFragment) {
//            (selectedFragment as RadioFragment).currEmotion = currEmotion
//            (selectedFragment as RadioFragment).currSong = currSong
//            (selectedFragment as RadioFragment).currArtist = currArtist
//            (selectedFragment as RadioFragment).currFaceId = currFaceGraphic
//        }
    }

//    fun playStation(uri: String) {
//        // Play a playlist
////        mSpotifyAppRemote!!.getPlayerApi().play("spotify:user:spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")
//        mSpotifyAppRemote!!.getPlayerApi().play(uri)
//        Log.d("Spotify", "Playing playlist")
//        println("Selected fragment")
//        println(selectedFragment)
//        if (selectedFragment is RadioFragment) {
//            print("IN HEEEERRRRREEEE")
//            selectedFragment.expression_graphic.setImageResource(R.drawable.happy_face)
//        }
//    }


    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

//    override fun onNewPlaylist(uri: String) {
//        playStation(uri)
//    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
        }
        try {
            Log.i(M_TAG, "Using Face Detector Processor")
            cameraSource?.setMachineLearningFrameProcessor(FaceDetectionProcessor())

        } catch (e: Exception) {
            Log.e(TAG, "can not create camera source")
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        cameraSource.let {
            try {
                if (firePreview == null) {
                    Log.d(M_TAG, "resume: Preview is null")
                }
                if (fireFaceOverlay == null) {
                    Log.d(M_TAG, "resume: graphOverlay is null")
                }
                firePreview.start(it!!, fireFaceOverlay)
            } catch (e: IOException) {
                Log.e(M_TAG, "Unable to start ML face detect camera source.", e)
                it?.release()
                cameraSource = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(M_TAG, "onResume starting ML Camera Source")
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        firePreview.stop()
    }

    public override fun onDestroy() {
        cameraSource?.release()
        super.onDestroy()
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        Log.d(M_TAG, "ML Kit all Permission granted")
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in requiredPermissions) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(it)
                }
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
        Log.d(M_TAG, "ML Kit got Runtime Permissions")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(M_TAG, "Permission granted!")
        if (allPermissionsGranted()) {
//            createCameraSource(selectedModel)
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object: Deejay.DeejayListener {
//        private var selectedFragment: Fragment = RadioFragment()
//        private var selectedFragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist)

        var mSpotifyAppRemote: SpotifyAppRemote? = null

        var CURR_EMOTION = "Passively"

        private var currFaceGraphic = R.drawable.passive_face
        private var currSong = ""
        private var currArtist = ""
        private var currAlbumCoverBitmap: Bitmap? = null
        private var currEmotion = CURR_EMOTION
//        private var selectedFragment: Fragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist,
//            currEmotion)
        private var selectedFragment = Fragment()

        override fun onNewPlaylist(arr: Array<String>) {
            if (mSpotifyAppRemote != null) {
                val uri = arr[0]
                val emotion = arr[1]
                CURR_EMOTION = emotionToAdverb(emotion)
                mSpotifyAppRemote!!.getPlayerApi().play(uri)
                //mSpotifyAppRemote!!.getPlayerApi().resume()
                Log.d("Spotify", "PLAYING PLAYLIST")

                val currPlay = URIToMusicMap[uri]
                if (selectedFragment is RadioFragment) {
                    selectedFragment.play_pause_btn.setBackgroundResource(android.R.drawable.ic_media_pause)
                    when (emotion) {
                        HAPPY -> selectedFragment.expression_graphic.setImageResource(R.drawable.happy_face)
                        PASSIVE -> selectedFragment.expression_graphic.setImageResource(R.drawable.passive_face)
                        SLEEPY -> selectedFragment.expression_graphic.setImageResource(R.drawable.sleepy_face)
                    }
                }
            }
        }

        const val HAPPY = "h"
        const val PASSIVE = "p"
        const val SLEEPY = "s"
        fun emotionToAdverb(emote: String): String {
            var ret_val = ""
            when (emote) {
                "h" -> ret_val = "Happily"
                "p" -> ret_val = "Passively"
                "s" -> ret_val = "Sleepily"
            }
            return ret_val
        }

        var currentUser: User? = null

        // Spotify
        private const val CLIENT_ID = "232e729656374c38b6df944ea2f39512"
        private const val REDIRECT_URI = "http://localhost:8888/callback"

        // ML Kit
        private const val TAG = "RadioActivity"
        private const val M_TAG = "MLKit - RadioActivity"
        private const val PERMISSION_REQUESTS = 1

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
