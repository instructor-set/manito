package com.instructor.manito

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.instructor.manito.databinding.ActivityLoginBinding
import com.instructor.manito.dto.User
import com.instructor.manito.lib.Authentication
import com.instructor.manito.lib.Database
import splitties.activities.start
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog

class LoginActivity : AppCompatActivity() {

    private val bind by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    // 로그인 방식 (구글, 페이스북 등)
    private val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build()
    )

    // 로그인 결과 콜백 등록
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    // 로그인 인텐트
    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .build()

    // 로그인 결과 콜백 함수
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {


        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {

            val userReference = Database.getReference("users/${Authentication.uid}")
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var user = snapshot.getValue<User>()
                    // 신규 로그인
                    if (user == null) {
                        val editText = EditText(this@LoginActivity)
                        // TODO 더 이쁘게
                        materialAlertDialog {
                            titleResource = R.string.title_dialog_nickname
                            // TODO 닉네임 조건 검사 추가
                            message = "닉네임 조건"
                            okButton {
                                val nickname = editText.text.toString()
                                user = User(nickname)
                                userReference.setValue(user)
                                successLogin(user!!)
                            }
                            cancelButton()

                        }.onShow {

                        }.run {
                            setView(editText)
                            show()
                        }
                    } else {
                        successLogin(user!!)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        } else {
            login()

        }
    }

    private fun successLogin(user: User) {
        Authentication.user = user
        materialAlertDialog {
            title = user.nickname
            message = "로그인 성공"
            okButton {
                bind.testAButton.isEnabled = true
            }
        }.show()
    }

    private fun login() {
        signInLauncher.launch(signInIntent)
    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // 로그아웃 이후 작업
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        with(bind) {
            if (Authentication.isLoggedIn()) {
                testAButton.isEnabled = true
            }
            testAButton.setOnClickListener {
                if (!Authentication.isLoggedIn()) {
                    testAButton.isEnabled = false
                } else {
                    start<MainActivity>()
                }
            }
            testBButton.setOnClickListener {
                login()
            }
            testCButton.setOnClickListener {
                testAButton.isEnabled = false
                logout()
            }

        }


    }
}