package com.instructor.manito.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.instructor.manito.databinding.ActivityLoginBinding
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
            val serverAccessToken = response.body()?.serverAccessToken
            // 회원 정보가 없음
            if (serverAccessToken == null) {

            } else {
                loginSuccess(serverAccessToken)
            }
        }

    }

    private fun loginSuccess(serverAccessToken: String) {
        bind.progressBar.visibility = View.VISIBLE
        Util.j(serverAccessToken)
//        Authentication.startMainActivity(this, accessToken) {
//            finish()
//        }
    }
}