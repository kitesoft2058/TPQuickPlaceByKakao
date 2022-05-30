package com.kitesoft.tpquickplacebykakao.activities

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.kitesoft.tpquickplacebykakao.R
import com.kitesoft.tpquickplacebykakao.databinding.ActivityMainBinding
import com.kitesoft.tpquickplacebykakao.fragments.ListFragment
import com.kitesoft.tpquickplacebykakao.fragments.MapFragment
import com.kitesoft.tpquickplacebykakao.model.KakaoSearchPlaceResponse
import com.kitesoft.tpquickplacebykakao.model.Place
import com.kitesoft.tpquickplacebykakao.model.PlaceMeta
import com.kitesoft.tpquickplacebykakao.network.RetrofitApiService
import com.kitesoft.tpquickplacebykakao.network.RetrofitHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.jar.Manifest

// 처음 앱 프로젝트 생성 후 package structure를 위해 package 폴더들 생성
// activities, fragments, adapters, model, network, ...
// 저 위 MainActivity의 package명을 직접 변경. 경고 밑줄이 보이면 [Move ... ]항목을 선택하여 자동으로 해당 패키지 밑으로 파일이동[드래그드롭으로 하면 안됨]
// 기본 패키지가 아닌 곳으로 이동했기에 기본패키지에 자동으로 만들어지는 Resource R 클래스가 import를 안하면 에러.
// res폴더도 하위폴더를 만들수는 있지만 번거롭고. 패키지는 변경할 수 없기에 결국 R 클래스를 사용하는 곳마다 import 해야함 [ view binding 하면 안할 수도 있음]

class MainActivity : AppCompatActivity() {

    val binding:ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    //카카오 검색에 필요한 요청 데이터 : query(검색장소명), x(경도:longitude), y(위도:latitude)
    //1. 검색장소명
    var searchQuery:String ="화장실"  //앱 초기 검색어 - 내 주변 개방 화장실
    //2. 현재 내위치 정보 객체 (위도,경도 정보를 멤버로 보유)
    var mylocation:Location?= null

    //[ Google Fused Location API 사용 :  play-services-location ]
    val providerClient: FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    //Kakao search API response object reference - ListFragment, MapFragment 모두 같은 데이터를 사용하므로 프레그먼트의 부모역할인 액티비티의 멤버에 데이터 위치.
    var searchPlaceResponse: KakaoSearchPlaceResponse?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 제목줄로 설정
        setSupportActionBar(binding.toolbar)

        //첫 실행될 프레그먼트 동적추가
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, ListFragment()).commit()

        //탭레이아웃의 탭버튼 클릭시에 보여줄 프레그먼트 변경
        binding.layoutTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.text=="LIST"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, ListFragment()).commit()
                }else if(tab?.text=="MAP"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, MapFragment()).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        //소프트키보드의 검색버튼 클릭하였을때.
        binding.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery= binding.etSearch.text.toString()
            searchPlace()

            //소프트키패드의 액션버튼이 클릭되었을때 여기서 모든 액션을 소모하지 않았다는 뜻으로 false 리턴. true 로 리턴하면 Editor 이벤트가 부모에게 전달되지 않음
            false
        }

        // 특정 키워드 단축 choice 버튼들에 리스너 처리하는 함수 호출
        setChoiceButtonsListener()

        //내 위치 정보제공은 사용자 동적퍼미션 필요
        val permissions:Array<String> = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if( checkSelfPermission(permissions[0])==PackageManager.PERMISSION_DENIED){
            requestPermissions(permissions, 10)
        }else{
            //위치정보에 대해 이미 허용한 적이 있다면 곧바로 내위치 요청기능 호출.
            requestMyLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==10 && grantResults[0]==PackageManager.PERMISSION_GRANTED) requestMyLocation() //내 위치 얻어오는 기능 함수 호출
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()

    }

    private fun requestMyLocation(){

        //위치검색 기준 설정값 객체
        val request:LocationRequest = LocationRequest.create()
        request.interval= 1000
        request.priority= LocationRequest.PRIORITY_HIGH_ACCURACY // 높은 정확도 우선


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {return
        }

        //실시간 위치정보 갱신 요청!
        providerClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

    }


    //위치정보 검색결과 콜백객체
    private val locationCallback: LocationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            mylocation = p0.lastLocation

            //위치 탐색이 끝났으니 내 위치 정보 업데이트는 이제 종료
            providerClient.removeLocationUpdates(this)  //this : locationCallback 객체

            //위치정보를 얻었으니 이제 검색 시작
            searchPlace()
        }
    }


    //카카오 키워드 로컬 검색 API 호출 메소드
    private fun searchPlace(){
        //Toast.makeText(this, "${searchQuery} : ${mylocation?.latitude} , ${mylocation?.longitude}", Toast.LENGTH_SHORT).show()

        //kakao keyword search api .. base url 레트로핏 겍체 생성
        val retrofit:Retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        val retrofitApiService= retrofit.create(RetrofitApiService::class.java)
        retrofitApiService.searchPlaceBy(searchQuery, mylocation?.longitude.toString(), mylocation?.latitude.toString()).enqueue(object : Callback<KakaoSearchPlaceResponse>{
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                searchPlaceResponse= response.body()

                //먼저 데이터가 온전히 잘 왔는지 파악하기 위해...
//                val meta:PlaceMeta?= searchPlaceResponse?.meta
//                val documents:MutableList<Place>? = searchPlaceResponse?.documents
//                AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count} \n ${documents?.get(0)?.place_name}").show()

                //무조건 검색이 완료되면 ListFragment 부터 보여주기 - 응답받은 결과 객체 전달해주기
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, ListFragment()).commit()

                //탭버튼의 위치를 ListFragment Tab 으로 변경
                binding.layoutTab.getTabAt(0)?.select()

            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "서버 오류가 있습니다.\n잠시뒤에 다시 시도해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            }
        })

//        retrofitApiService.searchPlaceByString(searchQuery, mylocation?.longitude.toString(), mylocation?.latitude.toString()).enqueue(object : Callback<String>{
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                AlertDialog.Builder(this@MainActivity).setMessage(response.body()).create().show()
//            }
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "서버 오류가 있습니다.\n잠시뒤에 다시 시도해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
//            }
//        })




    }


    //특정 키워드 단축 아이콘버튼들 클릭시에 반응하는 리스너 설정
    private fun setChoiceButtonsListener(){
        binding.layoutChoice.choiceWc.setOnClickListener { clickChoice(it) } //클릭된 버튼 뷰 객체를 clickChoice()메소드에 전달하여 클릭된 버튼을 구별
        binding.layoutChoice.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceGas.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePharmacy.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice1.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice2.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice3.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice4.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice5.setOnClickListener { clickChoice(it) }
    }


    //멤버변수(property)
    var choiceID = R.id.choice_wc  //선택된 키워드 단축 버튼 아이디 -초기 선택 wc

    private fun clickChoice(view: View){
        //기존 선택되었던 버튼을 찾아 배경이미지를 선택되지 않은 하얀색 원그림으로 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)

        //현재 클릭된 버튼(파라미터 : view)의 배경을 선택된 회색 원그림으로 변경
        view.setBackgroundResource(R.drawable.bg_choice_select)

        //다음 버튼 클릭시에 이전 클릭된 뷰의 ID를 기억하도록..[다음에 배경을 다시 하얀색 원그림으로 변경하기 위해]
        choiceID= view.id

        //초이스한 것에 따라 검색장소를 변경하여 다시 장소요청
        when(choiceID){
            R.id.choice_wc-> searchQuery="화장실"
            R.id.choice_movie-> searchQuery="영화관"
            R.id.choice_gas-> searchQuery="주유소"
            R.id.choice_ev-> searchQuery="전기차충전소"
            R.id.choice_pharmacy-> searchQuery="약국"
            R.id.choice_park-> searchQuery="공원"
            R.id.choice1-> searchQuery="맛집"
            R.id.choice2-> searchQuery="맛집"
            R.id.choice3-> searchQuery="맛집"
            R.id.choice4-> searchQuery="맛집"
            R.id.choice5-> searchQuery="맛집"
        }
        //새로운 검색 요청
        searchPlace()

        //검색창에 글씨가 있다면 지우기..
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus() //이전 포커스로 인해 커서가 남아있어서 포커스 없애기

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}