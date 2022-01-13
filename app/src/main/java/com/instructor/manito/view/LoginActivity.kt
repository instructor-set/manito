package com.instructor.manito.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.instructor.manito.databinding.ActivityLoginBinding
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Util
import com.instructor.manito.network.RetrofitClient
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManager
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import splitties.alertdialog.appcompat.*

class LoginActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        with(bind) {
            kakaoLoginButton.setOnClickListener {
                UserApiClient.instance.run {
                    if (isKakaoTalkLoginAvailable(this@LoginActivity)) {
                        loginWithKakaoTalk(this@LoginActivity, callback = kakaoLoginCallback)
                    } else {
                        loginWithKakaoAccount(this@LoginActivity, callback = kakaoLoginCallback)
                    }
                }
            }
            if (AuthApiClient.instance.hasToken()) {
                UserApiClient.instance.accessTokenInfo { _, error ->
                    if (error != null) {
                        needLogin()
                    }
                    // 카카오로 로그인이 되어있는 경우
                    else {
                        loggedInKakao()
                    }
                }
            } else {
                needLogin()
            }

        }


    }

    private val kakaoLoginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Util.j("로그인 실패: $error")
        } else if (token != null) {
            loggedInKakao()
        }
    }

    private fun needLogin() {
        bind.kakaoLoginButton.visibility = View.VISIBLE


    }

    private fun loggedInKakao() {
        Util.j("kakao 토큰: ${TokenManager.instance.getToken()?.accessToken}")
        RetrofitClient.kakaoLogin(TokenManager.instance.getToken()?.accessToken) { _, response ->
            val token = response.body()!!
            // 회원 정보가 없음
            auth.signInWithCustomToken(token.firebaseCustomAuthToken!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Util.j("signInWithCustomToken:success")
                        val user = auth.currentUser
                        loginSuccess(token.serverAccessToken!!)
                    } else {
                        // If sign in fails, display a message to the user.
                        Util.j("signInWithCustomToken:failure ${task.exception}")
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }

    private fun loginSuccess(serverAccessToken: String) {
        bind.progressBar.visibility = View.VISIBLE
        Authentication.serverAccessToken = serverAccessToken
    }
}