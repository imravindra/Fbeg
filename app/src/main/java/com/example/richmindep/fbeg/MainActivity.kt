package com.example.richmindep.fbeg

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.*


class MainActivity : AppCompatActivity() {
    private var googleSignIn: SignInButton? = null
    private var mGoogleSignInClient: GoogleApiClient? = null
    private val RC_SIGN_IN = 1234
    private var mAuth: FirebaseAuth? = null
    private var mAuthListner: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        mAuthListner = FirebaseAuth.AuthStateListener {
            if(it.currentUser!=null) {

                userUpdate()
            }else {
                heyTextView.text = "Log In To Continue!"
            }
        }
        googleSignIn = findViewById(R.id.signMeIn)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleApiClient.Builder(applicationContext)
                .enableAutoManage(this, GoogleApiClient.OnConnectionFailedListener {
                    Toast.makeText(this@MainActivity, "Connection failed!", Toast.LENGTH_SHORT).show()
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        signMeIn.setOnClickListener {
            signIn()
        }

        signMeOut.setOnClickListener {
            signMeOut()
        }
    }

    private fun signMeOut() {

        mAuth!!.signOut()
        heyTextView.text = "User Signed Out Successfully !"

    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListner!!)
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
          val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogleAccount(account)
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        userUpdate()
                        Toast.makeText(this@MainActivity, "Logged In Successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Authentication Failed!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun userUpdate() {
        val user = mAuth!!.currentUser
        heyTextView.text = "Hey "+user!!.displayName+"!"

    }


}