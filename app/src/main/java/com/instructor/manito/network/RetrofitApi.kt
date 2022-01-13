package com.instructor.manito.network

import com.instructor.manito.dto.Message
import com.instructor.manito.dto.Token
import com.instructor.manito.dto.UserEdit
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST


// 여기서 구현
interface RetrofitApi {

    @POST("/v1/auth/kakao/login")
    fun kakaoLogin(
        @Body
        token: Token
    ): Call<Token>

    @PATCH("/v1/user/me")
    fun editUser(
        @Header("Authorization")
        bearerAccessToken: String?,
        @Body
        userEdit: UserEdit
    ): Call<Message>


}