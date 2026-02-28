package com.example.reminderht5

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object GoogleAuth {

    fun getClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id được tạo tự động từ google-services.json
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(context: Context): Intent {
        return getClient(context).signInIntent
    }

    /**
     * Gọi hàm này khi bạn nhận được data từ ActivityResult (Google Sign-In)
     * - Thành công -> onSuccess(uid)
     * - Thất bại -> onError(message)
     */
    fun handleSignInResult(
        data: Intent?,
        onSuccess: (uid: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account: GoogleSignInAccount = try {
            task.getResult(ApiException::class.java)
        } catch (e: Exception) {
            onError(e.message ?: "Google Sign-In thất bại")
            return
        }

        val idToken = account.idToken
        if (idToken.isNullOrBlank()) {
            onError("Không lấy được idToken (kiểm tra SHA-1 + google-services.json)")
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid.isNullOrBlank()) onError("Đăng nhập xong nhưng uid rỗng")
                else onSuccess(uid)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Firebase Auth lỗi")
            }
    }

    fun signOut(context: Context, onDone: () -> Unit = {}) {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) { }
        getClient(context).signOut().addOnCompleteListener { onDone() }
    }

    fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid
}