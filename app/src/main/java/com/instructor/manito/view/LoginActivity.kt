package com.instructor.manito.view

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.instructor.manito.R
import com.instructor.manito.databinding.ActivityLoginBinding
import com.instructor.manito.dto.Message
import com.instructor.manito.dto.User
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import com.instructor.manito.lib.Util
import com.instructor.manito.network.RetrofitClient
import com.instructor.manito.view.login.MainActivity
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.TokenManager
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Response
import splitties.activities.start
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog

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
            Util.j(Utility.getKeyHash(this@LoginActivity))

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
            auth.signInWithCustomToken(token.firebaseCustomAuthToken!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Authentication.serverAccessToken = token.serverAccessToken
                        Database.getReference("users/${Authentication.uid}").get().addOnSuccessListener {
                            Authentication.user = it.getValue<User>()
                            if (Authentication.nickname.isNullOrEmpty()) {
                                val editText = EditText(this@LoginActivity)
                                // TODO 더 이쁘게
                                materialAlertDialog {
                                    titleResource = R.string.title_dialog_nickname
                                    // TODO 닉네임 조건 검사 추가
                                    message = "닉네임에는 공백이 포함될 수 없습니다."
                                    okButton {
                                        if(editText.text.isBlank()){
                                            Toast.makeText(context, "잘못된 닉네임입니다.", Toast.LENGTH_SHORT).show()
                                        } else{
                                            val nickname = editText.text.toString().replace(" ", "")
                                            RetrofitClient.editUser(nickname, onUnsuccessful = { _: Call<Message>, response: Response<Message> ->
                                                Util.j(response.message())
                                            }) { _, _ ->
                                                loginSuccess()
                                            }
                                        }

                                    }
                                    cancelButton()

                                }.onShow {

                                }.run {
                                    setView(editText)
                                    show()
                                }
                            } else {
                                loginSuccess()
                            }
                        }


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

    private fun loginSuccess() {
        bind.progressBar.visibility = View.VISIBLE
        start<MainActivity> {  }
    }
}