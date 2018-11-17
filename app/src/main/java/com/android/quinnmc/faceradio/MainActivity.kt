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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import com.android.quinnmc.faceradio.R.id.fireFaceOverlay
import com.android.quinnmc.faceradio.R.id.firePreview
import kotlinx.android.synthetic.main.activity_main.*

import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
//import jdk.nashorn.internal.runtime.ECMAException.getException
//import org.junit.experimental.results.ResultMatchers.isSuccessful
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import android.R.attr.password
import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.TextUtils.replace


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


class MainActivity() : AppCompatActivity(),
        ActivityCompat.OnRequestPermissionsResultCallback {
        //View.OnClickListener {

    // Firebase Auth
    //private lateinit var mAuth: FirebaseAuth

    // Spotify Will do Android's single-sign on, so end Spotify quick-start here
    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    // ML Kit vars
    private var cameraSource: CameraSource? = null
    private var selectedModel = FACE_DETECTION

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
        var selectedFragment: Fragment = RadioFragment()
        when (item.itemId) {
            R.id.navigation_home -> {
                //message.setText(R.string.title_home)
                selectedFragment = RadioFragment()
                //return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                //message.setText(R.string.title_dashboard)
                selectedFragment = SocialFragment()
                //return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                //message.setText(R.string.title_notifications)
                selectedFragment = SettingsFragment()
                //return@OnNavigationItemSelectedListener true
            }
        }
        supportFragmentManager.beginTransaction().
            replace(R.id.fragment_container, selectedFragment).commit()
        false
    }

    // Just get something from msgs

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Initialize Firebase Auth
//        mAuth = FirebaseAuth.getInstance()
//
//        signInButton.setOnClickListener(this)
//        signOutButton.setOnClickListener(this)

        //val options = arrayListOf(FACE_DETECTION)
//         Creating adapter for spinner
//        val dataAdapter = ArrayAdapter(this, R.layout.spinner_style, options)
//         Drop down layout style - list view with radio button
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//         attaching data adapter to spinner
//        spinner.adapter = dataAdapter
//        spinner.onItemSelectedListener = this
//
//        facingSwitch.setOnCheckedChangeListener(this)

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        } else {
            getRuntimePermissions()
        }

        // To remove the camera stream feedback
        firePreview.visibility = View.INVISIBLE
    }

    /***************************************************************
     ************  FIREBASE AUTH CODE STARTING HERE    *************
     ***************************************************************/

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            if (resultCode == Activity.RESULT_OK) {
//                // Sign in succeeded
//                updateUI(mAuth.currentUser)
//            } else {
//                // Sign in failed
//                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
//                updateUI(null)
//            }
//        }
//    }
//
//    private fun startSignIn() {
//        // Build FirebaseUI sign in intent. For documentation on this operation and all
//        // possible customization see: https://github.com/firebase/firebaseui-android
//        val intent = AuthUI.getInstance().createSignInIntentBuilder()
//            .setIsSmartLockEnabled(!BuildConfig.DEBUG)
//            .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
//            //.setLogo(R.mipmap.ic_launcher)
//            .build()
//
//        startActivityForResult(intent, RC_SIGN_IN)
//    }
//
//    private fun updateUI(user: FirebaseUser?) {
//        if (user != null) {
//            // Signed in
//            //status.text = getString(R.string.firebaseui_status_fmt, user.email)
//            //detail.text = getString(R.string.id_fmt, user.uid)
//
//            signInButton.visibility = View.GONE
//            signOutButton.visibility = View.VISIBLE
//        } else {
//            // Signed out
//            //status.setText("Signed out")
//            //status.setText(R.string.signed_out)
//            //detail.text = null
//
//            signInButton.visibility = View.VISIBLE
//            signOutButton.visibility = View.GONE
//        }
//    }
//
//    private fun signOut() {
//        AuthUI.getInstance().signOut(this)
//        updateUI(null)
//    }
//
//    override fun onClick(view: View) {
//        when (view.id) {
//            R.id.signInButton -> startSignIn()
//            R.id.signOutButton -> signOut()
//        }
//    }

    /**************************************************************
    ************    SPOTIFY CODE STARTING HERE    *****************
    ***************************************************************/

    override fun onStart() {
        super.onStart()

        // Firebase Auth
        // Check if user is signed in (non-null) and update UI accordingly.
        //val currentUser = mAuth.getCurrentUser()?.let {
        //updateUI(mAuth.currentUser)
//        } ?: {
//
//        }

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
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("Spotify", throwable.message, throwable)
                    //Log.e("ERROR", "ERROR connecting to Spotify")
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
    }

    fun connected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:user:spotify:playlist:37i9dQZF1DX2sUQwD7tbmL")
        Log.d("Spotify", "Playing playlist")

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
            .subscribeToPlayerState()
            .setEventCallback(object: Subscription.EventCallback<PlayerState> {

                override fun onEvent(playerState: PlayerState) {
                    val track = playerState.track
                    if (track != null) {
                        Log.d("SPOTIFY", "Current track: " + track.name + " by " + track.artist.name);
                    }
                }
            })
    }


    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }


    /**************************************************************
     ************    ML KIT CODE STARTING HERE    *****************
     ***************************************************************/


    // NO SPINNER AND TOGGLE

//    @Synchronized
//    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
//        // An item was selected. You can retrieve the selected item using
//        // parent.getItemAtPosition(pos)
//        selectedModel = parent.getItemAtPosition(pos).toString()
//        Log.d(TAG, "Selected model: $selectedModel")
//        //firePreview.stop()
//        if (allPermissionsGranted()) {
//            createCameraSource(selectedModel)
//            startCameraSource()
//        } else {
//            getRuntimePermissions()
//        }
//    }

//    override fun onNothingSelected(parent: AdapterView<*>) {
//        // Do nothing.
//    }

//    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
//        Log.d(TAG, "Set facing")
//        cameraSource?.let {
//            if (isChecked) {
//                it.setFacing(CameraSource.CAMERA_FACING_FRONT)
//            } else {
//                it.setFacing(CameraSource.CAMERA_FACING_BACK)
//            }
//        }
//        //firePreview.stop()
//        startCameraSource()
//    }

    private fun createCameraSource(model: String) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = CameraSource(this, fireFaceOverlay)
        }

        try {
            cameraSource?.let {
                when (model) {
                    FACE_DETECTION -> {
                        Log.i(M_TAG, "Using Face Detector Processor")
                        it.setMachineLearningFrameProcessor(FaceDetectionProcessor())
                    }
                    else -> Log.e(M_TAG, "Unknown ML model: $model")
                }
            }
        //} catch (e: FirebaseMLException) {
        } catch (e: Exception) {
            Log.e(TAG, "can not create camera source: $model")
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
        super.onDestroy()
        cameraSource?.release()
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
            createCameraSource(selectedModel)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        // Firebase Auth
        //private const val RC_SIGN_IN = 9001
        // Spotify
        private const val CLIENT_ID = "232e729656374c38b6df944ea2f39512"
        private const val REDIRECT_URI = "http://localhost:8888/callback" // help
        // ML Kit
        private const val FACE_DETECTION = "Face Detection"
        private const val TAG = "MainActivity"
        private const val M_TAG = "MLKit - MainActivity"
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
