package com.example.rateflow

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInHelper(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient

    init {
        // Configure Google Sign In with your client ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("253632602120-fh2ff76jia74rc9m62kpvagcn2pmlrf2.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?): GoogleSignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            GoogleSignInResult.Success(account)
        } catch (e: ApiException) {
            GoogleSignInResult.Error(e)
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
    }

    fun revokeAccess() {
        googleSignInClient.revokeAccess()
    }

    sealed class GoogleSignInResult {
        data class Success(val account: GoogleSignInAccount) : GoogleSignInResult()
        data class Error(val exception: ApiException) : GoogleSignInResult()
    }
}