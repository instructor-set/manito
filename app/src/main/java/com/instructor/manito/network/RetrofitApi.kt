package com.instructor.manito.network

import com.instructor.manito.dto.Token
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


// 여기서 구현
interface RetrofitApi {

    @POST("/v1/auth/kakao/login")
    fun kakaoLogin(
        @Body
        token: Token
    ): Call<Token>


}