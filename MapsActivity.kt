package com.example.mapsmylocation

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mapsmylocation.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>//권한 승인 요청을 위한 런처..한번에 2개의 권한 승인을 요청하기 때문에 Array..

    private lateinit var fusedLocationClient: FusedLocationProviderClient//위칫값 사용 위해..
    private lateinit var locationCallback: LocationCallback//위칫값 요청에 대한 갱신 정보 받는 데 필요함.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ results ->//런처 생성
            if(results.all{it.value}){
                startProcess()
            }else{
                Toast.makeText(this, "권한 승인이 필요합니다.", Toast.LENGTH_LONG).show()
            }
        }
        locationPermission.launch(//2개의 권한을 파라미터에 전달
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    fun startProcess(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)//위치 검색 클라이언트 생성
        updateLocation()
    }
    @SuppressLint("MissingPermission")//fusedLocationClient.requestLocationUpdates 는 권한 처리가 필요한데 현재 코드에서는 확인할 수 없으므로 해당 코드를 체크하지 않아도 된다는 애너테이션 달아줌.
    private fun updateLocation() {
        val locationRequest = LocationRequest.create()//위치 정보 요청
        locationRequest.run{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY//위치정보 정확도
            interval = 1000//위치정보 요청 주기..1초
        }

        locationCallback = object : LocationCallback(){//주기마다 반환받는다...1초
            override fun onLocationResult(locationResult: LocationResult?) {//1초 마다 변환된 위치 정보가 전달된다.
                locationResult?.let{
                    for((i, location) in it.locations.withIndex()){
                        Log.d("Location", "$i ${location.latitude}, ${location.longitude}")
                        setLastLocation(location)//변환된 위치정보 전달
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    fun setLastLocation(lastLocation: Location){//위치 정보를 받아서 마커를 그리고 화면을 이동.
        val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)   //전달 받은 위치 정보로 좌표 생성
        val markerOptions = MarkerOptions()//마커 생성
            .position(LATLNG)
            .title("Here!")

        val cameraPosition = CameraPosition.Builder()//카메라 위치를 현재 위치로 세팅
            .target(LATLNG)
            .zoom(15.0f)
            .build()

        mMap.clear()//이전에 그려진 마커 지우기
        mMap.addMarker(markerOptions)//마커 추가
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}