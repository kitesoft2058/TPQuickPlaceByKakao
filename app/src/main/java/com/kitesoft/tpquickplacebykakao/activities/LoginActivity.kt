package com.kitesoft.tpquickplacebykakao.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.kitesoft.tpquickplacebykakao.G
import com.kitesoft.tpquickplacebykakao.databinding.ActivityLoginBinding
import com.kitesoft.tpquickplacebykakao.model.NidUserInfoResponse
import com.kitesoft.tpquickplacebykakao.model.UserAccount
import com.kitesoft.tpquickplacebykakao.network.RetrofitApiService
import com.kitesoft.tpquickplacebykakao.network.RetrofitHelper
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        setContentView(binding.root)

        //둘러보기 글씨 클릭으로 로그인없이 Main화면 실행
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //회원가입 버튼 클릭
        binding.tvSignup.setOnClickListener {
            //회원가입화면으로 전환
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //이메일 로그인 버튼 클릭
        binding.layoutMail.setOnClickListener {
            //이메일로 로그인 화면 전환
            startActivity(Intent(this, EmailSignInActivity::class.java))
        }

        //간편로그인 버튼들 클릭
        binding.btnLoginKakao.setOnClickListener { clickLoginKakao() }
        binding.btnLoginGoogle.setOnClickListener { clickLoginGoogle() }
        binding.btnLoginNaver.setOnClickListener { clickLoginNaver() }


        //카카오 키해시 얻어오기 [ Google Play Store 에 앱배포 후 release 키해시 추가 등록 필요 ]
        var keyHash = Utility.getKeyHash(this)
        Log.i("keyHash", keyHash)
    }

    private fun clickLoginKakao(){

        //build.gradle: Android Studio Artic Fox(최신) 외 버전
        //settings.gradle: Android Studio Artic Fox(최신) 버전 이후...

        // 카카오 로그인 공통 callback 구성 *******************************************************
//        val callback= fun(token:OAuthToken?, error:Throwable?){
//        }
        val callback:(OAuthToken?, Throwable?)->Unit = { token, error ->
            if(error != null){
                Toast.makeText(this, "카카오로그인 실패", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "카카오로그인 성공", Toast.LENGTH_SHORT).show()

                //사용자 정보 요청
                UserApiClient.instance.me { user, error ->
                    if(user!=null){
                        var id:String= user.id.toString()
                        var email:String= user.kakaoAccount?.email ?: "" //혹시 null 이면 이메일의 기본값은 ""

                        Toast.makeText(this, "$email", Toast.LENGTH_SHORT).show()
                        G.userAccount= UserAccount(id, email)

                        //main 화면으로 전환.
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }//***********************************************************************************

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        }else{
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun clickLoginGoogle(){

        //Firebase Authentication(인증) - ID 공급업체 통합 ( 구글로그인 ) 사용
        //[주의!! Firebase에서 구글 이메일 인증을 켜 주셨는지 확인 !!!!!!]

        // 가이드문서가 google login을 firebase와 연동하는 형태로만 소개되어서 수업시간 자료와 다름. 확인 필요. --  firebase-auth sdk 추가설치는 필수아님.

        // 구글에 검색 [ 안드로이드 구글 로그인 구현 ] -- https://developers.google.com/identity/sign-in/android/start-integrating
        // or 구글에 검색 [ google identity sign in android ]

        //Google 계정을 이용한 간편로그인 기능
        //단, Google계정 로그인 SDK를 별도 추가 해야함. play-services-auth 라이브러리
        // - 구글로그인 화면(액티비티)을 실행시키는 Intent 를 통한 startActivityForResult 로그인 방법

        // 아래 ServerClientId 설정값.
        //a. GCP Console에서 사용자 인증 정보 페이지를 엽니다.
        //b. 웹 애플리케이션 유형의 클라이언트 ID가 백엔드 서버의 OAuth 2.0 클라이언트 ID입니다.
        // [ 추후 release 했을때. firebase에 play store sha-1 추가 필요.]

        //구글 로그인 옵션객체 생성 - Builder 이용
        val signInOptions: GoogleSignInOptions= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                    //.requestIdToken("174859039037-ud8otqvb5fhui85hs16av9iafpupg5g2.apps.googleusercontent.com")
                                                    .requestEmail()
                                                    .build()

        //구글 로그인 화면 액티비티를 실행하는 Intent 객체 얻어오기
        val intent:Intent= GoogleSignIn.getClient(this, signInOptions).signInIntent
        resultLauncher.launch(intent)
    }

    //구글 로그인 화면 액티비티를 실행시키고 그 결과를 받아오는 startActivityForResult를 실행하는 객체를 액티비티에 등록 및 생성
    val resultLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult>{
        override fun onActivityResult(result: ActivityResult?) {
            //로그인 결과를 가져온 인텐트 객체 소환
            val intent:Intent?= result?.data
            //Intent로 부터 구글 계정 정보를 가져오는 작업 객체 생성
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)

            val account:GoogleSignInAccount= task.result
            var id:String= account.id.toString()
            var email:String= account.email ?: "" //혹시 null 이면 이메일의 기본값은 ""

            Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
            G.userAccount= UserAccount(id, email)

            //main 화면으로 전환.
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    })


    private fun clickLoginNaver(){

        //네이버 아이디 로그인 [네아로] . developers.naver.com. - 사용자 정보를 REST API로 받아오는 방식
        //개발가이드 [ 모바일앱 > Android ] 참고.   , 개발가이드의 [로그인버튼 사용가이드]를 통해 버튼 이미지 다운로드 가능.

        //Nid-OAuth: 네아로 SDK 적용
        //가이드의 [Maven Repository 사용하기] 의 Gradle 스크립트 작성하는 방법이 현재는 에러 없음. aar 다운로드 방식은 현재 에러가 있음.


        //먼저 개발자 사이트에서 앱등록 및 설정. 하여 ClientID, ClientSecret 발급.
        //다음으로 네아로 객체 초기화
        NaverIdLoginSDK.initialize(this, "V2AbictL_HjbKK8A2ROk", "0NYx5XfWy9", "퀵플")
        //OAUTH_CLIENT_ID: 애플리케이션 등록 후 발급받은 클라이언트 아이디
        //OAUTH_CLIENT_SECRET: 애플리케이션 등록 후 발급받은 클라이언트 시크릿
        //OAUTH_CLIENT_NAME: 네이버 앱의 로그인 화면에 표시할 애플리케이션 이름. 모바일 웹의 로그인 화면을 사용할 때는 서버에 저장된 애플리케이션 이름이 표시됩니다.

        //로그인 방법은 2가지 제공.
        // 네이버 전용 로그인 버튼 NidOAuthLoginButton 사용      - UI가 정해져 있음.
        // NaverIdLoginSDK.authenticate() 메서드를 직접 실행   - 원하는 버튼 이미지를 사용할 수 있음. [이 방법 선택]

        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback{
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "error : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "failure : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                // 사용자 정보를 가져오는 REST API를 작업할때 접속토큰(access token)이 필요함.
                val accessToken:String?= NaverIdLoginSDK.getAccessToken()

                //Toast.makeText(this@LoginActivity, "$accessToken", Toast.LENGTH_SHORT).show()
                Log.i("token", accessToken+"");

                //Retrofit 작업을 통해 사용자 정보 가져오기.
                // [ 가이드 문서의 API명세 > 회원프로필조희 API명세 ] 참고. 언어별 코드 설명 아래쪽으로 API 기본정보 및 요청파라미터와 응답필드가 소개되어 있음.
                val retrofit= RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")
                retrofit.create(RetrofitApiService::class.java).getNidUserInfo("Bearer $accessToken").enqueue(object : Callback<NidUserInfoResponse>{
                    override fun onResponse(
                        call: Call<NidUserInfoResponse>,
                        response: Response<NidUserInfoResponse>
                    ) {
                        val userInfo: NidUserInfoResponse?= response.body()
                        val id:String= userInfo?.response?.id ?: ""
                        val email:String= userInfo?.response?.email ?: ""

                        Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                        G.userAccount= UserAccount(id, email)

                        //main 화면으로 전환.
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }

                    override fun onFailure(call: Call<NidUserInfoResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "회원정보 불러오기 실패 : ${t.message}", Toast.LENGTH_SHORT).show()
                    }

                })

            }

        })








    }
}