package com.instructor.manito

import android.app.Application
import com.instructor.manito.lib.Param
import com.instructor.manito.lib.Util
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, Param.KAKAO_SDK_APP_KEY)
        Util.j(Utility.getKeyHash(this))
    }
}