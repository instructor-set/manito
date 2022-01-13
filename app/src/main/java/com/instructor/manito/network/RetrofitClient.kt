package com.instructor.manito.network

import com.instructor.manito.dto.Token
import com.instructor.manito.lib.Param
import com.instructor.manito.lib.Util
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private val instance = Retrofit.Builder().baseUrl(Param.SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitApi::class.java)

    fun kakaoLogin(
        kakaoAccessToken: String?,
        onUnsuccessful: ((Call<Token>, Response<Token>) -> Unit)? = null,
        onSuccessful: (Call<Token>, Response<Token>) -> Unit
    ) = instance.kakaoLogin(Token(kakaoAccessToken = kakaoAccessToken)).enqueue(
        if (onUnsuccessful == null) {
            defaultCallback(onSuccessful = onSuccessful)
        } else {
            defaultCallback(onUnsuccessful, onSuccessful)
        }
    )



    private fun <T> defaultCallback(
        onUnsuccessful: (Call<T>, Response<T>) -> Unit = { _, response ->
            Util.j("실패: $response")
        },
        onSuccessful: (Call<T>, Response<T>) -> Unit
    ) = object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                onSuccessful(call, response)
            } else {
                onUnsuccessful(call, response)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            Util.j("Fail: ${t.message}")
        }

    }

}