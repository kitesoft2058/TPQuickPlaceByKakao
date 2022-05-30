package com.kitesoft.tpquickplacebykakao.network

import com.kitesoft.tpquickplacebykakao.model.KakaoSearchPlaceResponse
import com.kitesoft.tpquickplacebykakao.model.NidUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {

    //네아로 사용자정보 API
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization: String): Call<NidUserInfoResponse>

    //카카오 키워드 장소검색 API .. response type - String
    @Headers("Authorization: KakaoAK b05f34637c7abe8c131a3d4d25add016")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaceByString(@Query("query")query: String, @Query("x") longitude:String,@Query("y") latitude:String) : Call<String>

    //카카오 키워드 장소검색 API .. response type - KakaoSearchPlaceResponse
    @Headers("Authorization: KakaoAK b05f34637c7abe8c131a3d4d25add016")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaceBy(@Query("query")query: String, @Query("x") longitude:String,@Query("y") latitude:String) : Call<KakaoSearchPlaceResponse>


}