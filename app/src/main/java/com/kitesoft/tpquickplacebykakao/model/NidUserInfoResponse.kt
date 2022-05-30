package com.kitesoft.tpquickplacebykakao.model

data class NidUserInfoResponse(var resultcode:String, var message:String, var response: NidUser)

data class NidUser(var id:String, var email:String) //필요한 응답필드만 사용.
