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
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.view.Menu
import android.view.MenuItem
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
 *  ADDITIONAL INFO FOR MILESTONE 3:
 *  Setup before-hand to use this app successfully,
 *  1) Have Spotify installed on the running device, and be logged in w/in the Spotify app
 *          -- making a non-Premium account works fine, if you don't already have one
 *  2) If using the emulator, configure your AVD camera's front and back to be Webcam0 (laptop's camera)
 *
 *  * Unfortunate emulator bug I couldn't get around w/o this workaround:
 *      For MLKit to detect your face,
 *      you MUST tilt either your head 90 deg right or webcam 90 left
 *
 *  (MLKit activity should be seen by filtering "MLKit" in Logcat)
 *  (Bug to know: Now can only detect a face in one of first frames received,
 *      but you should be able to see something like this: (copy/pasted from my Logcat))
 *   D/MLKit - Face Det Proc: HAPPINESS PROBABILITY = 0.029783526
 *   D/MLKit - Face Det Proc: LEFT EYE OPEN PROBABILITY = 0.92879176
 *   D/MLKit - Face Det Proc: RIGHT EYE OPEN PROBABILITY = 0.6806507
 *
 *  Milestone 3 functionality summary:
 *  Log you into firebase, play music after sign in,
 *  start attempting to read your face, let you go to each page
 *
 *  From profile and msg activites, can get back with the system back-button
 *  Bug: initial fragment "radio" doesn't appear until you go to another fragment and back
 *
 * **********************************/


class RadioActivity() : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback {//,
        //Deejay.DeejayListener {

//    // Spotify Will do Android's single-sign on, so end Spotify quick-start here
//    private var mSpotifyAppRemote: SpotifyAppRemote? = null

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
//        selectedFragment = RadioFragment()
        when (item.itemId) {
            R.id.navigation_home -> {
//                selectedFragment = RadioFragment()
                selectedFragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist)

                supportFragmentManager.beginTransaction().
                    replace(R.id.fragment_container, selectedFragment).commit()


                // Invoke findViewById on null object reference
//                selectedFragment?.current_track_label.text = currSong
//                selectedFragment?.current_artist_label.text = currArtist
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

        // Comment out to expose firepreview
//        selectedFragment = RadioFragment()
        supportFragmentManager.beginTransaction().
            replace(R.id.fragment_container, selectedFragment!!).commit()
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

    /**************************************************************
    ************    SPOTIFY CODE STARTING HERE    *****************
    ***************************************************************/

    override fun onStart() {
        super.onStart()

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

                    // Now you can start interacting with App Remote

                    // TEMPORARY - connected() PLAYS MUSIC!
                    //playStation()
                    mSpotifyAppRemote!!.getPlayerApi().setShuffle(true)

                    // TODO: play and pause buttons
//                    mSpotifyAppRemote!!.getPlayerApi().pause()
//                    mSpotifyAppRemote!!.getPlayerApi().skipNext()
//                    mSpotifyAppRemote!!.getPlayerApi().skipPrevious()


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

        private var currFaceGraphic = R.drawable.passive_face
        private var currSong = ""
        private var currArtist = ""
        private var selectedFragment: Fragment = RadioFragment.newInstance(currFaceGraphic, currSong, currArtist)


        private var mSpotifyAppRemote: SpotifyAppRemote? = null

        override fun onNewPlaylist(arr: Array<String>) {
            if (mSpotifyAppRemote != null) {
                val uri = arr[0]
                val emotion = arr[1]
                CURR_EMOTION = emotionToAdverb(emotion)
                mSpotifyAppRemote!!.getPlayerApi().play(uri)
                Log.d("Spotify", "Playing playlist")

                //selectedFragment.expression_graphic.visibility = View.VISIBLE
//                selectedFragment.current_track_title.visibility = View.VISIBLE
//                selectedFragment.current_track_label.visibility = View.VISIBLE
//                selectedFragment.by_title.visibility = View.VISIBLE
//                selectedFragment.current_artist_label.visibility = View.VISIBLE

                val currPlay = URIToMusicMap[uri]
                if (selectedFragment is RadioFragment) {
                    when (emotion) {
                        HAPPY -> selectedFragment.expression_graphic.setImageResource(R.drawable.happy_face)
                        PASSIVE -> selectedFragment.expression_graphic.setImageResource(R.drawable.passive_face)
                        SLEEPY -> selectedFragment.expression_graphic.setImageResource(R.drawable.sleepy_face)
                    }
                }
            }
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

//        var CURR_URI: String? = null
        const val HAPPY = "h"
        const val PASSIVE = "p"
        const val SLEEPY = "s"
        var CURR_EMOTION = "Passively"
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
