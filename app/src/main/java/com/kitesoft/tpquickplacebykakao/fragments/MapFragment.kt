package com.kitesoft.tpquickplacebykakao.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.kitesoft.tpquickplacebykakao.R
import com.kitesoft.tpquickplacebykakao.activities.MainActivity
import com.kitesoft.tpquickplacebykakao.activities.PlaceUrlActivity
import com.kitesoft.tpquickplacebykakao.databinding.FragmentListBinding
import com.kitesoft.tpquickplacebykakao.databinding.FragmentMapBinding
import com.kitesoft.tpquickplacebykakao.databinding.MapMakerBalloonBinding
import com.kitesoft.tpquickplacebykakao.model.KakaoSearchPlaceResponse
import com.kitesoft.tpquickplacebykakao.model.Place
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MapFragment : Fragment() {

    val binding:FragmentMapBinding by lazy { FragmentMapBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    // Kakao Map SDK - developers.kakao.com 가이드 문서 참고.
    // .zip 파일 다운받아 압축풀고.
    // libDaumMapAndroid.jar은 /app/libs/에 복사 그리고 libMapEngineApi.so 파일은 /app/source/main/jniLibs로 아키텍쳐별 디렉토리 하에 복사
    // * 중요! libs폴더의 .jar 파일은 build.gradle에 의존성 라이브러리로 추가해줘야 함.

    val mapView:MapView by lazy { MapView(context) } //맵뷰객체 생성- 늦은 초기화. 아직 프레그먼트가 액티비티에 붙기 전이기에..멤버에서 바로 생성 불가

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.containerMapview.addView(mapView)

        // 마커 or 말풍선의 클릭이벤트에 반응하는 리스너 등록 - 반드시 마커 추가보다 먼저 등록되어 있어야 적용됨. [우선 마커들 부터 추가한 후 실습]
        mapView.setPOIItemEventListener(markerEventListener)

        // 커스텀 말풍선(Callout Balloon) - 반드시 마커들이 추가되기 전에 설정되어야 커스텀 말풍선이 적용됨
        mapView.setCalloutBalloonAdapter(CustomBallonAdapter(layoutInflater))

        // 지도관련 설정( 지도위치, 마커추가 등...)
        setMapAndMarkers()
    }

    private fun setMapAndMarkers(){
        // 맵 중심점 변경
        // 현재 내 위치 위도경도좌표 객체(kakao sdk) 생성 - 내 위치는 MainActivity의 멤버에 저장되어 았음 [ 좌표값이 null일때를 위해 Elvis 연산자로 기본값 지정 ]
        var lat: Double= (activity as MainActivity).mylocation?.latitude ?: 37.5666805
        var lng: Double= (activity as MainActivity).mylocation?.longitude ?: 126.9784147

        var myMapPoint: MapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
        mapView.setMapCenterPointAndZoomLevel(myMapPoint, 5,true)
        mapView.zoomIn(true)
        mapView.zoomOut(true)

        //내 위치 마커 추가
        val marker= MapPOIItem()
        marker.apply {
            itemName="ME"
            mapPoint= myMapPoint
            markerType= MapPOIItem.MarkerType.BluePin
            selectedMarkerType= MapPOIItem.MarkerType.YellowPin
        }
        mapView.addPOIItem(marker)


        // 검색결과 장소 마커들 추가
        val documents: MutableList<Place>? = (activity as MainActivity).searchPlaceResponse?.documents
        documents?.forEach {
            val point:MapPoint= MapPoint.mapPointWithGeoCoord(it.y.toDouble(), it.x.toDouble())

            //마커옵션 객체를 만들어 기본 설정. - 스코프함수 apply 람다의 마지막 리턴이 본인 객체임
            val maker:MapPOIItem = MapPOIItem().apply {
                mapPoint= point
                itemName= it.place_name
                markerType= MapPOIItem.MarkerType.RedPin
                selectedMarkerType= MapPOIItem.MarkerType.YellowPin
                //해당 POI Item(마커)과 관련된 정보를 저장하고 있는 임의의 객체를 저장
                userObject= it
            }
            mapView.addPOIItem(maker)
        }//////////////////////////////////////////////////////////////////////////

    }


    //마커나 말풍선이 클릭되는 이벤트에 반응하는 리스너클래스 //////////////////////////////////////////////////////
    private val markerEventListener:MapView.POIItemEventListener= object : MapView.POIItemEventListener{
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
            //마카 클릭시에 발동
            //Toast.makeText(context, "a", Toast.LENGTH_SHORT).show()
        }

        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
            //deprecated... 아래 매소드 사용을 권장
        }

        //마커의 말풍선을 클릭시에 발동
        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {
            //Toast.makeText(context, "b", Toast.LENGTH_SHORT).show()
            //두번째파라미터 p1 : 마커객체
            //해당 마커객체에게 설정한 사용자정보객체를 얻어오기
            if(p1?.userObject == null ) return

            val place: Place = p1?.userObject as Place

            //place_url를 얻어와서 PlaceUrlActivity 실행하면서 넘겨주기
            val intent: Intent = Intent(context, PlaceUrlActivity::class.java)
            intent.putExtra("place_url", place.place_url)
            startActivity(intent) //프레그먼트의 startActivity() 메소드 사용.
        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
            //Toast.makeText(context, "c", Toast.LENGTH_SHORT).show()
        }
    }///////////////////////////////////////////////////////////////////////////////////////////////



    //커스텀 말풍선 클래스
    inner class CustomBallonAdapter constructor(inflater: LayoutInflater) : CalloutBalloonAdapter{

        val binding: MapMakerBalloonBinding by lazy { MapMakerBalloonBinding.inflate(inflater) }

        //마커 클릭시 보여질 말풍선의 뷰 리턴 [ 마치 프레그먼트의 onCreateView()와 같은 역할
        override fun getCalloutBalloon(p0: MapPOIItem?): View {
            binding.tvBalloonTitle.text= p0?.itemName

            //해당 마커에게 저장한 사용자 정보객체 중 distance 얻어오기
            if(p0?.userObject != null ) binding.tvBallonDistance.text= (p0.userObject as Place).distance + "m"
            return binding.root
        }

        //마커 클릭시 - 말풍선이 보이면서 호출됨
        override fun getPressedCalloutBalloon(p0: MapPOIItem?): View {
            //별도 Thread로 동작하기에 toast 같은 UI변경작업은 못함 - 별도의 POIEventListener를 설정할 것이기에 필요없음
            //Log.i("AAA", "clicked")
            return binding.root
        }
    }

}