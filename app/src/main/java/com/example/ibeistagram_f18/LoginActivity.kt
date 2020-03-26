package com.example.ibeistagram_f18

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {



    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var callbackManager : CallbackManager ? = null
    var GOOGLE_LOGIN_CODE = 9001
    /*default_web_client_id
    639211856591-va4ebl16g2o380g32f0ju0jds04fbrot.apps.googleusercontent.com
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener {
            signinAndsignup()
        }
        google_sign_in_button.setOnClickListener{
            googlelogin()
        }
        facebook_login_button.setOnClickListener{
            //fist step
            facebooklogin()
        }
        /*
        If I place it here, then the var is not able to use library.
        (GoogleSignInOptions cannot be activated here)
        **** notice *****
        if, default_web_client_id cannot be found, go get the api in the gradle to get the web client id.
        639211856591-va4ebl16g2o380g32f0ju0jds04fbrot.apps.googleusercontent.com
         */

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
//        printHashKey()
    }
    //  s/49uynbyTyt6s+hUKJaQYAO84w=

    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
            // Framg
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    fun googlelogin(){
        var singInIntent = googleSignInClient?.signInIntent
        startActivityForResult(singInIntent,GOOGLE_LOGIN_CODE)
    }

    fun facebooklogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                 // second step : 성공하면 firebase에 토큰을 넘겨줌
                handleFacebookAccessToken(result?.accessToken)
                    // I forget to put the function handdleFacebookAccessToken and there was an error saying "Unexpected CallbackManager, please use the provided Factory."
                }

                override fun onCancel() {
                    // "TO DO" code causes error so make sure delete them before run the program
                }

                override fun onError(error: FacebookException?) {

                }

            })
    }

    fun handleFacebookAccessToken(token : AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // Third step : Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message, login failure
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess){
                var account = result.signInAccount
                //Second Step
                firebaseAuthWithGoogle(account)
            }
        }
    }
    fun firebaseAuthWithGoogle( account: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message, login failure
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }

            }
    }
    fun signinAndsignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener{
            task ->
                if(task.isSuccessful){
                    //creating a user account
                    moveMainPage(task.result?.user)
                }else if(task.exception?.message.isNullOrEmpty()){
                   //show the login error message
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }else{
                    //login if you have account
                    signinEmail()
                }

        }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(),password_edittext.text.toString())
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    //Login
                    moveMainPage(task.result?.user)
                }else{
                    //Show the error message, login failure
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }

            }
    }
    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}
