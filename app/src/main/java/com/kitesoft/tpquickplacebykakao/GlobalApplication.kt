package com.kitesoft.tpquickplacebykakao

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        //kakao init
        KakaoSdk.init(this, "09a57701100919ff69f2da58575d9a24")
    }
}