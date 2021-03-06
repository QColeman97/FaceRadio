package com.android.quinnmc.faceradio

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.notification_template_lines_media.view.*
import java.util.*


class LoginActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Buttons
        email_sign_in_button.setOnClickListener(this)
        email_create_account_button.setOnClickListener(this)
        select_photo_btn.setOnClickListener {
            Log.d("LoginActivity", "Selecting photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]
    }

    var selected_photo_uri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("LoginActivity", "Photo was selected")

            selected_photo_uri = data.data // Location where image stored on device
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selected_photo_uri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            select_photo_btn.setBackgroundDrawable(bitmapDrawable)
        }
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        //updateUI(currentUser)
    }
    // [END on_start_check_user]

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                    uploadImgToFirebaseStorage()

                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

                hideProgressDialog()
            }
    }

    private fun uploadImgToFirebaseStorage() {
        if (selected_photo_uri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selected_photo_uri!!).addOnSuccessListener {
            Log.d("LoginActivity", "Successfully uploaded image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                it.toString()
                Log.d("LoginActivity", "File location: $it")

                saveUserToFirebaseDatabase(it.toString())
            }
        }
        .addOnFailureListener {
            // Do some logging here
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username.text.toString(), profileImageUrl,
            arrayListOf<String>(), arrayListOf<String>(), arrayListOf<String>(), "", "")
        ref.setValue(user).addOnSuccessListener {
            Log.d("LoginActivity", "Finally saved user to Firebase Database")

            // OLD
            //val main_intent = Intent(this, RadioActivity::class.java)
            val main_intent = Intent(this, SettingsActivity::class.java)

            // Old - back-button exits
            //main_intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(main_intent)
        }
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Firebase signInWithEmail:success")
                    val user = auth.currentUser
                    // intent
                    val main_intent = Intent(this, RadioActivity::class.java)
                    startActivity(main_intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Firebase signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                hideProgressDialog()
            }
    }

    private fun signOut() {
        auth.signOut()
        //updateUI(null)
    }

//    private fun sendEmailVerification() {
//        // Disable button
//        verifyEmailButton.isEnabled = false
//
//        // Send verification email
//        // [START send_email_verification]
//        val user = auth.currentUser
//        user?.sendEmailVerification()
//            ?.addOnCompleteListener(this) { task ->
//                // [START_EXCLUDE]
//                // Re-enable button
//                verifyEmailButton.isEnabled = true
//
//                if (task.isSuccessful) {
//                    Toast.makeText(baseContext,
//                        "Verification email sent to ${user.email} ",
//                        Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e(TAG, "sendEmailVerification", task.exception)
//                    Toast.makeText(baseContext,
//                        "Failed to send verification email.",
//                        Toast.LENGTH_SHORT).show()
//                }
//                // [END_EXCLUDE]
//            }
//        // [END send_email_verification]
//    }

    private fun validateForm(): Boolean {
        var valid = true

        val email_txt = email.text.toString()
        if (TextUtils.isEmpty(email_txt)) {
            email.error = "Required."
            valid = false
        } else {
            email.error = null
        }

        val password_txt = password.text.toString()
        if (TextUtils.isEmpty(password_txt)) {
            password.error = "Required."
            valid = false
        } else {
            password.error = null
        }

        return valid
    }

//    private fun updateUI(user: FirebaseUser?) {
//        hideProgressDialog()
//        if (user != null) {
//            //status.text = getString(R.string.emailpassword_status_fmt,
//                user.email, user.isEmailVerified)
//            //detail.text = getString(R.string.firebase_status_fmt, user.uid)
//
//            emailPasswordButtons.visibility = View.GONE
//            emailPasswordFields.visibility = View.GONE
//            signedInButtons.visibility = View.VISIBLE
//
//            verifyEmailButton.isEnabled = !user.isEmailVerified
//        } else {
//            //status.setText(R.string.signed_out)
//            //detail.text = null
//
//            emailPasswordButtons.visibility = View.VISIBLE
//            emailPasswordFields.visibility = View.VISIBLE
//            signedInButtons.visibility = View.GONE
//        }
//    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.email_create_account_button -> createAccount(email.text.toString(), password.text.toString())
            R.id.email_sign_in_button -> signIn(email.text.toString(), password.text.toString())
            //R.id.signOutButton -> signOut()
            //R.id.verifyEmailButton -> sendEmailVerification()
        }
    }

    companion object {
        private const val TAG = "EmailPasswordLogin"
    }
}





/**
 * A login screen that offers login via email/password.
 */
//class LoginActivity : AppCompatActivity(), LoaderCallbacks<Cursor> {
//    /**
//     * Keep track of the login task to ensure we can cancel it if requested.
//     */
//    private var mAuthTask: UserLoginTask? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//        // Set up the login form.
//        populateAutoComplete()
//        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
//            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                attemptLogin()
//                return@OnEditorActionListener true
//            }
//            false
//        })
//
//        email_sign_in_button.setOnClickListener { attemptLogin() }
//    }
//
//    private fun populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return
//        }
//
//        loaderManager.initLoader(0, null, this)
//    }
//
//    private fun mayRequestContacts(): Boolean {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(email, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                .setAction(android.R.string.ok,
//                    { requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS) })
//        } else {
//            requestPermissions(arrayOf(READ_CONTACTS), REQUEST_READ_CONTACTS)
//        }
//        return false
//    }
//
//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete()
//            }
//        }
//    }
//
//
//    /**
//     * Attempts to sign in or register the account specified by the login form.
//     * If there are form errors (invalid email, missing fields, etc.), the
//     * errors are presented and no actual login attempt is made.
//     */
//    private fun attemptLogin() {
//        if (mAuthTask != null) {
//            return
//        }
//
//        // Reset errors.
//        email.error = null
//        password.error = null
//
//        // Store values at the time of the login attempt.
//        val emailStr = email.text.toString()
//        val passwordStr = password.text.toString()
//
//        var cancel = false
//        var focusView: View? = null
//
//        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
//            password.error = getString(R.string.error_invalid_password)
//            focusView = password
//            cancel = true
//        }
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(emailStr)) {
//            email.error = getString(R.string.error_field_required)
//            focusView = email
//            cancel = true
//        } else if (!isEmailValid(emailStr)) {
//            email.error = getString(R.string.error_invalid_email)
//            focusView = email
//            cancel = true
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView?.requestFocus()
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true)
//            mAuthTask = UserLoginTask(emailStr, passwordStr)
//            mAuthTask!!.execute(null as Void?)
//        }
//    }
//
//    private fun isEmailValid(email: String): Boolean {
//        //TODO: Replace this with your own logic
//        return email.contains("@")
//    }
//
//    private fun isPasswordValid(password: String): Boolean {
//        //TODO: Replace this with your own logic
//        return password.length > 4
//    }
//
//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private fun showProgress(show: Boolean) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
//
//            login_form.visibility = if (show) View.GONE else View.VISIBLE
//            login_form.animate()
//                .setDuration(shortAnimTime)
//                .alpha((if (show) 0 else 1).toFloat())
//                .setListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        login_form.visibility = if (show) View.GONE else View.VISIBLE
//                    }
//                })
//
//            login_progress.visibility = if (show) View.VISIBLE else View.GONE
//            login_progress.animate()
//                .setDuration(shortAnimTime)
//                .alpha((if (show) 1 else 0).toFloat())
//                .setListener(object : AnimatorListenerAdapter() {
//                    override fun onAnimationEnd(animation: Animator) {
//                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
//                    }
//                })
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            login_progress.visibility = if (show) View.VISIBLE else View.GONE
//            login_form.visibility = if (show) View.GONE else View.VISIBLE
//        }
//    }
//
//    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
//        return CursorLoader(
//            this,
//            // Retrieve data rows for the device user's 'profile' contact.
//            Uri.withAppendedPath(
//                ContactsContract.Profile.CONTENT_URI,
//                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
//            ), ProfileQuery.PROJECTION,
//
//            // Select only email addresses.
//            ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(
//                ContactsContract.CommonDataKinds.Email
//                    .CONTENT_ITEM_TYPE
//            ),
//
//            // Show primary email addresses first. Note that there won't be
//            // a primary email address if the user hasn't specified one.
//            ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
//        )
//    }
//
//    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
//        val emails = ArrayList<String>()
//        cursor.moveToFirst()
//        while (!cursor.isAfterLast) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS))
//            cursor.moveToNext()
//        }
//
//        addEmailsToAutoComplete(emails)
//    }
//
//    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {
//
//    }
//
//    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        val adapter = ArrayAdapter(
//            this@LoginActivity,
//            android.R.layout.simple_dropdown_item_1line, emailAddressCollection
//        )
//
//        email.setAdapter(adapter)
//    }
//
//    object ProfileQuery {
//        val PROJECTION = arrayOf(
//            ContactsContract.CommonDataKinds.Email.ADDRESS,
//            ContactsContract.CommonDataKinds.Email.IS_PRIMARY
//        )
//        val ADDRESS = 0
//        val IS_PRIMARY = 1
//    }
//
//    /**
//     * Represents an asynchronous login/registration task used to authenticate
//     * the user.
//     */
//    inner class UserLoginTask internal constructor(private val mEmail: String, private val mPassword: String) :
//        AsyncTask<Void, Void, Boolean>() {
//
//        override fun doInBackground(vararg params: Void): Boolean? {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000)
//            } catch (e: InterruptedException) {
//                return false
//            }
//
//            return DUMMY_CREDENTIALS
//                .map { it.split(":") }
//                .firstOrNull { it[0] == mEmail }
//                ?.let {
//                    // Account exists, return true if the password matches.
//                    it[1] == mPassword
//                }
//                ?: true
//        }
//
//        override fun onPostExecute(success: Boolean?) {
//            mAuthTask = null
//            showProgress(false)
//
//            if (success!!) {
//                finish()
//            } else {
//                password.error = getString(R.string.error_incorrect_password)
//                password.requestFocus()
//            }
//        }
//
//        override fun onCancelled() {
//            mAuthTask = null
//            showProgress(false)
//        }
//    }
//
//    companion object {
//
//        /**
//         * Id to identity READ_CONTACTS permission request.
//         */
//        private val REQUEST_READ_CONTACTS = 0
//
//        /**
//         * A dummy authentication store containing known user names and passwords.
//         * TODO: remove after connecting to a real authentication system.
//         */
//        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
//    }
//}
