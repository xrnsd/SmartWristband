package com.kuyou.smartwristband;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceLocation;
import com.kuyou.smartwristband.gps.GPSUtils;
import com.kuyou.smartwristband.gps.filter.kalman.TrajectoryKalmanFilter;
import com.kuyou.smartwristband.gps.filter.TrajectoryFilter;
import com.kuyou.smartwristband.gps.filter.TrajectoryFluctuationFilter;
import com.kuyou.smartwristband.gps.filter.TrackPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * 轨迹纠偏功能 示例 使用起来更简单
 */
public class TraceActivity_Simple extends Activity {
    private List<TraceLocation> mListPoint = new ArrayList<>();
    private List<TraceLocation> originPosList;
    private LBSTraceClient lbsTraceClient;

    private static final String TAG = "TraceActivity_Simple_123456";
    private int posCount = 0;
    private TraceLocation posTraceLocation;


    private final Timer timer = new Timer();


    private MapView mMapView;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_simple);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        netChangReceiver = new NetChangReceiver();
        registerReceiver(netChangReceiver, new IntentFilter("location_guiji"));
    }

    /**
     * 初始化
     */
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.getUiSettings().setRotateGesturesEnabled(false);
            aMap.getUiSettings().setZoomControlsEnabled(false);
        }
        initView();
        //.setDDD(this);
        mPolylineOptions.width(10).color(Color.argb(255, 1, 1, 1));
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                latLngs2.clear();
                TrackPoint point;
                List<LatLng> latLngs = getList();
                List<TrackPoint> trackPointList = new ArrayList<>();
                Log.d("Trajectory2", "==================== 基础数据长度=" + latLngs.size());
                int index = 0;
                double[] ddd;

                Location locationOld = null, locationNow = null;

                for (LatLng lacation : getList()) {
                    locationNow = new Location(LocationManager.GPS_PROVIDER);

                    point = new TrackPoint();
                    ddd = GPSUtils.gps84_To_Gcj02(lacation.latitude, lacation.longitude);
                    lacation = new LatLng(ddd[0], ddd[1]);
                    latLngs.set(index, lacation);
                    locationNow.setLatitude(ddd[0]);
                    locationNow.setLongitude(ddd[1]);
                    point.setLatitude(ddd[0]);
                    point.setLongitude(ddd[1]);
                    if (null != locationOld) {
                        point.setBearing(locationNow.bearingTo(locationOld));
                    }
                    locationOld = locationNow;
                    point.setTime(latLngsTimes[index++] * 1000);
                    trackPointList.add(point);
                }
                aMap.addPolyline(new PolylineOptions().
                        addAll(latLngs).width(10).color(Color.RED));
                initTrajectoryFilters(trackPointList);
            }
        }, 3000);
//        getWindow().getDecorView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                List<LatLng> latLngs = new ArrayList<>();
//                Log.d("TraceActivity_Simple",
//                       "getFilterTrackBaseAll size=" + WristbandManager.getInstance(null, null).getWristbandDevice().getFilterTrackBaseAll().size());
//                for (TrackPoint point : WristbandManager.getInstance(null, null).getWristbandDevice().getFilterTrackBaseAll()) {
//                    latLngs.add(new LatLng(point.getLatitude(),point.getLongitude()));
//                }
//                aMap.addPolyline(new PolylineOptions().
//                        addAll(latLngs).width(10).color(Color.RED));
//                latLngs.clear();
//                Log.d("TraceActivity_Simple",
//                        "getFilterTrackFilterAll size=" + WristbandManager.getInstance(null, null).getWristbandDevice().getFilterTrackFilterAll().size());
//                for (TrackPoint point2 : WristbandManager.getInstance(null, null).getWristbandDevice().getFilterTrackFilterAll()) {
//                    latLngs.add(new LatLng(point2.getLatitude(),point2.getLongitude()));
//                }
//                aMap.addPolyline(new PolylineOptions().
//                        addAll(latLngs).width(10).color(Color.BLACK));
//
//            }
//        }, 3000);

    }


    public void dddd(TrackPoint point) {
        runOnUiThread(() -> dddd3(point));
    }

    private void dddd3(TrackPoint point) {

//        MarkerOptions markerOption = new MarkerOptions();
//        markerOption.position(startLocation_gps);
//        markerOption.draggable(false);//设置Marker可拖动
//        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                .decodeResource(getResources(), R.drawable.marker_blue)));
//        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//        markerOption.setFlat(true);//设置marker平贴地图效果
//        aMap.addMarker(markerOption);

        aMap.addPolyline(new PolylineOptions().
                addAll(getList()).width(10).color(Color.argb(255, 1, 1, 1)));

//        MarkerOptions markerOption ;
//        for (LatLng startLocation_gps:latLngs ){
//            markerOption = new MarkerOptions();
//            markerOption.position(startLocation_gps);
//            markerOption.draggable(false);//设置Marker可拖动
//            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                    .decodeResource(getResources(), R.drawable.marker_blue)));
//            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//            markerOption.setFlat(true);//设置marker平贴地图效果
//            aMap.addMarker(markerOption);
//        }
    }

    List<TrackPoint> mTrackPointList = new ArrayList<>();
    final List<LatLng> latLngs2 = new ArrayList<>();
    private TrajectoryFilter mAngleFilter, mAngleFilter2, mAngleFilter3, mDisTanceFilter, mSpeedFilter;
    PolylineOptions mPolylineOptions = new PolylineOptions();
    int mDataAfterFilterAngle = 0;
    int mDataAfterFilterDisTance = 0;
    int mDataAfterFilterKaman = 0;
    TrajectoryKalmanFilter mKalmanLocationService;

    private void initTrajectoryFilters(List<TrackPoint> list) {
        mDataAfterFilterAngle = 0;
        mDataAfterFilterDisTance = 0;
        mDataAfterFilterKaman = 0;
        if (null == mSpeedFilter) {
            mKalmanLocationService = new TrajectoryKalmanFilter(TraceActivity_Simple.this.getApplicationContext(),
                    new TrajectoryKalmanFilter.OnDataFilterListener() {
                        @Override
                        public void onDataAfterFilter(TrackPoint point) {
                            Log.d("KalmanLocationService", "onDataAfterFilter=====================");
                            MarkerOptions markerOption = new MarkerOptions();
                            LatLng startLocation_gps = new LatLng(point.getLatitude(), point.getLongitude());
                            markerOption.position(startLocation_gps);
                            markerOption.draggable(false);//设置Marker可拖动
                            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                    .decodeResource(getResources(), R.drawable.marker_blue)));
                            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                            markerOption.setFlat(true);//设置marker平贴地图效果
                            aMap.addMarker(markerOption);
                        }
                    });
        }
        //mKalmanLocationService.filter(list);
    }

    private void drawTraceLine(List<TrackPoint> list) {
        {
            mTrackPointList.addAll(list);
            for (TrackPoint point : list) {
                latLngs2.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            Log.d("Trajectory2", "===================  latLngs2.sze()=" + latLngs2.size());
            if (latLngs2.size() >= 0) {
                final List<LatLng> latLngs = new ArrayList<>();
                latLngs.addAll(latLngs2);
                latLngs2.clear();

                aMap.addPolyline(new PolylineOptions().
                        addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));

                int index = 0;
                MarkerOptions markerOption;
                for (LatLng startLocation_gps : latLngs) {
                    markerOption = new MarkerOptions();
                    markerOption.position(startLocation_gps);
                    markerOption.title(new StringBuilder("点")
                            .append(index).append(":").append(mTrackPointList.get(index).getAngle())
                            .toString());
                    markerOption.draggable(false);//设置Marker可拖动
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.marker_blue)));
                    // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                    markerOption.setFlat(true);//设置marker平贴地图效果
                    aMap.addMarker(markerOption);
                    index += 1;
                }


                mDataAfterFilterAngle = 0;
                mDataAfterFilterDisTance = 0;
                mDataAfterFilterKaman = 0;
                latLngs.clear();
                mTrackPointList.clear();
            }
        }
    }


    NetChangReceiver netChangReceiver;

    class NetChangReceiver extends BroadcastReceiver {

        //重写onReceive方法，该方法的实体为，接收到广播后的执行代码；
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getFloatExtra("Latitude", 0f);
            double longitude = intent.getFloatExtra("Longitude", 0f);
            if (0 == latitude
                    || 0 == longitude) {
                Log.e("123456789", "NetChangReceiver > 信息为空");
                return;
            }

            Log.d("123456789", "NetChangReceiver > 绘制轨迹点");

//            LatLng startLocation_gps = new LatLng(latitude, longitude);
//            MarkerOptions markerOption = new MarkerOptions();
//            markerOption.position(startLocation_gps);
//            markerOption
//            markerOption.draggable(false);//设置Marker可拖动
//            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                    .decodeResource(getResources(), R.drawable.marker_blue)));
//            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//            markerOption.setFlat(true);//设置marker平贴地图效果
//            aMap.addMarker(markerOption);

            posCount++;

            //if (posCount == TrackPointList.length-1)
            // timer.cancel();
        }
    }

    public void initView() {

//		BitmapDescriptor mPoint = BitmapDescriptorFactory.fromResource(R.drawable.start);
//		// 设置司机定位点
//		MyLocationStyle myLocationStyle;
//		myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
//		myLocationStyle.myLocationIcon(mPoint);
//		myLocationStyle.myLocationType(LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//		myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//		aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//		aMap.getUiSettings().setMyLocationButtonEnabled(true); // 设置默认定位按钮是否显示，非必需设置。
//		aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//
//		lbsTraceClient = LBSTraceClient.getInstance(this);

        if (originPosList == null) {
            originPosList = new ArrayList<>();
            LatLng positionLatLng = new LatLng(22.624991f, 113.865306f);
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, 16));
        }

//        getWindow().getDecorView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("123456789","sendBroadcast");
//                Intent intent=new Intent("location_guiji");
//                sendBroadcast(intent);
//            }
//        },2000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        timer.cancel();
        unregisterReceiver(netChangReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    public static List<LatLng> getList() {
        List<LatLng> latLngs = new ArrayList<LatLng>();

        latLngs.add(new LatLng(22.565337272855558f, 114.05300706296718f));
        latLngs.add(new LatLng(22.56532427863312f, 114.05314550934331f));
        latLngs.add(new LatLng(22.56532418542648f, 114.05316317995997f));
        latLngs.add(new LatLng(22.56531734443749f, 114.05317128357987f));
        latLngs.add(new LatLng(22.565321915665248f, 114.05320256036022f));
        latLngs.add(new LatLng(22.56536381647941f, 114.05322711991228f));
        latLngs.add(new LatLng(22.56534527683408f, 114.05323283426425f));
        latLngs.add(new LatLng(22.56535710682472f, 114.05322378060843f));
        latLngs.add(new LatLng(22.56535726300742f, 114.05322364259031f));
        latLngs.add(new LatLng(22.56535726321067f, 114.05322364240664f));
        latLngs.add(new LatLng(22.56535726321815f, 114.05322364239962f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966966808f, 114.05314630800186f));
        latLngs.add(new LatLng(22.56535796696491f, 114.05314630888365f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565357966964886f, 114.05314630888373f));
        latLngs.add(new LatLng(22.565394656586037f, 114.05329915834126f));
        latLngs.add(new LatLng(22.56539270682932f, 114.05329313518618f));
        latLngs.add(new LatLng(22.565341036380282f, 114.05206827845748f));
        latLngs.add(new LatLng(22.565358150308796f, 114.05248790176722f));
        latLngs.add(new LatLng(22.565357823485748f, 114.05089228053754f));
        latLngs.add(new LatLng(22.565357741227256f, 114.0505367626067f));
        latLngs.add(new LatLng(22.565357735847286f, 114.05049705854994f));
        latLngs.add(new LatLng(22.56558083769292f, 114.04775671778089f));
        latLngs.add(new LatLng(22.56558195978232f, 114.04774615917486f));
        latLngs.add(new LatLng(22.565181867931827f, 114.04434282689297f));
        latLngs.add(new LatLng(22.56519393363344f, 114.04440513474117f));
        latLngs.add(new LatLng(22.5645937430327f, 114.04037588395082f));
        latLngs.add(new LatLng(22.564593131585042f, 114.04035940744126f));
        latLngs.add(new LatLng(22.56336340516077f, 114.03865051784084f));
        latLngs.add(new LatLng(22.563247020946516f, 114.03572735311234f));
        latLngs.add(new LatLng(22.563260493923437f, 114.03605659889516f));
        latLngs.add(new LatLng(22.562735265934574f, 114.03350498416245f));
        latLngs.add(new LatLng(22.562736447450938f, 114.03363715120939f));
        latLngs.add(new LatLng(22.56265529133411f, 114.03246692733958f));
        latLngs.add(new LatLng(22.562640624118576f, 114.03227584409265f));
        latLngs.add(new LatLng(22.562399589094102f, 114.03124423760308f));
        latLngs.add(new LatLng(22.562376927783426f, 114.03116792398403f));
        latLngs.add(new LatLng(22.56237340624697f, 114.03114884693623f));
        latLngs.add(new LatLng(22.562418297044488f, 114.03086968722808f));
        latLngs.add(new LatLng(22.56241825105435f, 114.03086991763777f));
        latLngs.add(new LatLng(22.562138682302816f, 114.02932384410225f));
        latLngs.add(new LatLng(22.562136229729933f, 114.02931305197163f));
        latLngs.add(new LatLng(22.56187847207106f, 114.02774624091685f));
        latLngs.add(new LatLng(22.561878191162887f, 114.02772760043057f));
        latLngs.add(new LatLng(22.56151502540881f, 114.02584043897293f));
        latLngs.add(new LatLng(22.561514342460804f, 114.02583829131945f));
        latLngs.add(new LatLng(22.56151434114249f, 114.02583829635942f));
        latLngs.add(new LatLng(22.560838693077994f, 114.02261089196853f));
        latLngs.add(new LatLng(22.56096597533752f, 114.02326741273882f));
        latLngs.add(new LatLng(22.56096531551022f, 114.02326596330144f));
        latLngs.add(new LatLng(22.560888346531648f, 114.02273306811858f));
        latLngs.add(new LatLng(22.560887751347302f, 114.02273005934313f));
        latLngs.add(new LatLng(22.561349841164812f, 114.02143860778368f));
        latLngs.add(new LatLng(22.560632816985294f, 114.01939889402996f));
        latLngs.add(new LatLng(22.560641282600795f, 114.01943411155632f));
        latLngs.add(new LatLng(22.560635910865685f, 114.01942349637004f));
        latLngs.add(new LatLng(22.560636024230078f, 114.01942366801761f));
        latLngs.add(new LatLng(22.56063615206652f, 114.01942386157764f));
        latLngs.add(new LatLng(22.560400013311536f, 114.01475543323758f));
        latLngs.add(new LatLng(22.560412252655542f, 114.01497443144852f));
        latLngs.add(new LatLng(22.560411962862737f, 114.01496074378025f));
        latLngs.add(new LatLng(22.560411555345713f, 114.01495540316431f));
        latLngs.add(new LatLng(22.55927580132186f, 114.01319304432785f));
        latLngs.add(new LatLng(22.559288482555367f, 114.01320256580819f));
        latLngs.add(new LatLng(22.559334854560316f, 114.0107249055467f));
        latLngs.add(new LatLng(22.559335008734575f, 114.01071407826922f));
        latLngs.add(new LatLng(22.55836101319485f, 114.00882366753912f));
        latLngs.add(new LatLng(22.558542972253466f, 114.00914746503683f));
        latLngs.add(new LatLng(22.556549238821837f, 114.00842936367113f));
        latLngs.add(new LatLng(22.556768877806984f, 114.00850219025767f));
        latLngs.add(new LatLng(22.556059268428776f, 114.00426931902474f));
        latLngs.add(new LatLng(22.555702031569442f, 114.00191691490465f));
        latLngs.add(new LatLng(22.55570060955221f, 114.00190929137794f));
        latLngs.add(new LatLng(22.55570065548111f, 114.00190937536601f));
        latLngs.add(new LatLng(22.55570071686217f, 114.00190958894788f));
        latLngs.add(new LatLng(22.5557007795776f, 114.00190980717284f));
        latLngs.add(new LatLng(22.555700850299253f, 114.0019100532563f));
        latLngs.add(new LatLng(22.5581420613671f, 113.99112982573982f));
        latLngs.add(new LatLng(22.558132019079064f, 113.99116303868826f));
        latLngs.add(new LatLng(22.559574701534554f, 113.9901262764142f));
        latLngs.add(new LatLng(22.5593812400742f, 113.99025460161653f));
        latLngs.add(new LatLng(22.559976256966664f, 113.98481198652829f));
        latLngs.add(new LatLng(22.55981753965716f, 113.98631236199876f));
        latLngs.add(new LatLng(22.55981785770117f, 113.98630970143881f));
        latLngs.add(new LatLng(22.55981751763267f, 113.98631259217777f));
        latLngs.add(new LatLng(22.558022045656575f, 113.98133878470674f));
        latLngs.add(new LatLng(22.55817881554369f, 113.9817826163104f));
        latLngs.add(new LatLng(22.55747059543156f, 113.97778527881324f));
        latLngs.add(new LatLng(22.557586362119167f, 113.97846996159565f));
        latLngs.add(new LatLng(22.557584006543422f, 113.97845980767165f));
        latLngs.add(new LatLng(22.55671283996449f, 113.9759331831401f));
        latLngs.add(new LatLng(22.5567021383186f, 113.97588986231261f));
        latLngs.add(new LatLng(22.556535462440145f, 113.97341156667815f));
        latLngs.add(new LatLng(22.556534763868417f, 113.97340309335708f));
        latLngs.add(new LatLng(22.557536789970275f, 113.97125081773802f));
        latLngs.add(new LatLng(22.55753013894412f, 113.97124623158054f));
        latLngs.add(new LatLng(22.5579492637576f, 113.96962456141813f));
        latLngs.add(new LatLng(22.557944013718103f, 113.9696417624953f));
        latLngs.add(new LatLng(22.557949427334933f, 113.96962408660067f));
        latLngs.add(new LatLng(22.557949230307976f, 113.96962470600647f));
        latLngs.add(new LatLng(22.55794914482015f, 113.96962500810959f));
        latLngs.add(new LatLng(22.557949059332323f, 113.9696253102127f));
        latLngs.add(new LatLng(22.558165609474f, 113.96664581124995f));
        latLngs.add(new LatLng(22.55815997574608f, 113.96671691857688f));
        latLngs.add(new LatLng(22.558160237053723f, 113.96671393870949f));
        latLngs.add(new LatLng(22.558160228493037f, 113.96671402677042f));
        latLngs.add(new LatLng(22.555616355458753f, 113.96525234641757f));
        latLngs.add(new LatLng(22.555924463155755f, 113.96545053906378f));
        latLngs.add(new LatLng(22.55590308103042f, 113.96543913854669f));
        latLngs.add(new LatLng(22.556307379482938f, 113.96263578012355f));
        latLngs.add(new LatLng(22.55631260956133f, 113.96258745635045f));
        latLngs.add(new LatLng(22.555526522823193f, 113.96119275007749f));
        latLngs.add(new LatLng(22.555545297827788f, 113.96121455539843f));
        latLngs.add(new LatLng(22.567283336133737f, 114.04550067562494f));
        latLngs.add(new LatLng(22.566882122770867f, 114.04351297939924f));
        latLngs.add(new LatLng(22.556112635352704f, 113.95363741149306f));
        latLngs.add(new LatLng(22.55626442715044f, 113.95499274287808f));
        latLngs.add(new LatLng(22.556038345803394f, 113.94896244163871f));
        latLngs.add(new LatLng(22.556043839242836f, 113.9491686241855f));
        latLngs.add(new LatLng(22.556094708107466f, 113.94670567822715f));
        latLngs.add(new LatLng(22.556083045235237f, 113.94724110210853f));
        latLngs.add(new LatLng(22.55608301466845f, 113.94724236034436f));
        latLngs.add(new LatLng(22.556083007759586f, 113.94724255194107f));
        latLngs.add(new LatLng(22.55561049486212f, 113.94177552977904f));
        latLngs.add(new LatLng(22.555618051083698f, 113.94184805484316f));
        latLngs.add(new LatLng(22.555618073538305f, 113.94184824215434f));
        latLngs.add(new LatLng(22.555618073550434f, 113.94184824237652f));
        latLngs.add(new LatLng(22.555675460519215f, 113.94076844798434f));
        latLngs.add(new LatLng(22.555661639754355f, 113.94103531626412f));
        latLngs.add(new LatLng(22.55566163681769f, 113.9410353389987f));
        latLngs.add(new LatLng(22.55566631260444f, 113.94065607343306f));
        latLngs.add(new LatLng(22.555666364831325f, 113.9406528530862f));
        latLngs.add(new LatLng(22.556114239457457f, 113.9405736192918f));
        latLngs.add(new LatLng(22.556087580041343f, 113.94057687547375f));
        latLngs.add(new LatLng(22.55608793381898f, 113.94057687844838f));
        latLngs.add(new LatLng(22.55608846665317f, 113.94057683633865f));
        latLngs.add(new LatLng(22.55608898815046f, 113.94057679512487f));
        latLngs.add(new LatLng(22.555907169747833f, 113.94040080179673f));
        latLngs.add(new LatLng(22.555642472504303f, 113.9403276412524f));
        latLngs.add(new LatLng(22.555677395604928f, 113.94034152018797f));
        latLngs.add(new LatLng(22.555874273293636f, 113.93824990361924f));
        latLngs.add(new LatLng(22.555832999739735f, 113.9386933897826f));
        latLngs.add(new LatLng(22.555858367600703f, 113.93810221266958f));
        latLngs.add(new LatLng(22.555857686596674f, 113.93813278096383f));
        latLngs.add(new LatLng(22.555868440579175f, 113.93679141929343f));
        latLngs.add(new LatLng(22.555868220701168f, 113.936853947449f));
        latLngs.add(new LatLng(22.555868300280476f, 113.93684366221906f));
        latLngs.add(new LatLng(22.55585156517922f, 113.93551732360388f));
        latLngs.add(new LatLng(22.555851576448838f, 113.93551806221333f));
        latLngs.add(new LatLng(22.5557418279497f, 113.93473599179865f));
        latLngs.add(new LatLng(22.555737945248044f, 113.93470797994614f));
        latLngs.add(new LatLng(22.559159653294316f, 113.9675600172599f));
        latLngs.add(new LatLng(22.558966766357127f, 113.96612026260074f));
        latLngs.add(new LatLng(22.55897723185927f, 113.96619717942494f));
        latLngs.add(new LatLng(22.558989622886056f, 113.96628467763088f));
        latLngs.add(new LatLng(22.566200372395286f, 113.92853353675004f));
        latLngs.add(new LatLng(22.565805944029407f, 113.93061908012773f));
        latLngs.add(new LatLng(22.565651999079897f, 113.93124822760299f));
        latLngs.add(new LatLng(22.56553303480653f, 113.93178874489716f));
        latLngs.add(new LatLng(22.565414070533166f, 113.93232926219133f));
        latLngs.add(new LatLng(22.565297637414552f, 113.93285827911754f));
        latLngs.add(new LatLng(22.55559902501251f, 113.92225312817767f));
        latLngs.add(new LatLng(22.555668477507222f, 113.9223174850034f));
        latLngs.add(new LatLng(22.555662299570656f, 113.92231274704581f));
        latLngs.add(new LatLng(22.556065363846105f, 113.92052300498607f));
        latLngs.add(new LatLng(22.556067243814233f, 113.92051656490104f));
        latLngs.add(new LatLng(22.555241124723437f, 113.91997698201018f));
        latLngs.add(new LatLng(22.55529054424363f, 113.92000043073735f));
        latLngs.add(new LatLng(22.55611008694523f, 113.91971658343506f));
        latLngs.add(new LatLng(22.556074854327377f, 113.91972586460722f));
        latLngs.add(new LatLng(22.556060529330534f, 113.91701388640203f));
        latLngs.add(new LatLng(22.556064150214375f, 113.91770418438809f));
        latLngs.add(new LatLng(22.55606405166961f, 113.9176829120292f));
        latLngs.add(new LatLng(22.55606405066739f, 113.91768345348326f));
        latLngs.add(new LatLng(22.555393656529528f, 113.91462200149905f));
        latLngs.add(new LatLng(22.554638818468167f, 113.91327442422148f));
        latLngs.add(new LatLng(22.554654219448814f, 113.91330122023348f));
        latLngs.add(new LatLng(22.554637480892758f, 113.91327244763465f));
        latLngs.add(new LatLng(22.55463404384536f, 113.91326779021725f));
        latLngs.add(new LatLng(22.554630532079543f, 113.91326303155164f));
        latLngs.add(new LatLng(22.554626497284772f, 113.9132575641486f));
        latLngs.add(new LatLng(22.554622985518954f, 113.91325280548298f));
        latLngs.add(new LatLng(22.554619473753135f, 113.91324804681737f));
        latLngs.add(new LatLng(22.554615961987317f, 113.91324328815176f));
        latLngs.add(new LatLng(22.554612300784655f, 113.91323832698974f));
        latLngs.add(new LatLng(22.554608789018836f, 113.91323356832413f));
        latLngs.add(new LatLng(22.551924362652997f, 113.90808122550325f));
        latLngs.add(new LatLng(22.551920760928297f, 113.90807791249804f));
        latLngs.add(new LatLng(22.556043132655926f, 113.9051722920071f));
        latLngs.add(new LatLng(22.555562039155838f, 113.90551347801056f));
        latLngs.add(new LatLng(22.555699815759226f, 113.90564604275046f));
        latLngs.add(new LatLng(22.555662245883436f, 113.90560994114449f));
        latLngs.add(new LatLng(22.557258241766394f, 113.90355316604837f));
        latLngs.add(new LatLng(22.5570871745946f, 113.90376755202207f));
        latLngs.add(new LatLng(22.558891608657245f, 113.90233143135468f));
        latLngs.add(new LatLng(22.558941272526877f, 113.90230595373303f));
        latLngs.add(new LatLng(22.558955254335483f, 113.90229686674002f));
        latLngs.add(new LatLng(22.559834595440037f, 113.90067164007603f));
        latLngs.add(new LatLng(22.559850110316166f, 113.9006440603779f));
        latLngs.add(new LatLng(22.561815395858183f, 113.89887388017208f));
        latLngs.add(new LatLng(22.561822987915708f, 113.89886810857739f));
        latLngs.add(new LatLng(22.562581707323986f, 113.8978642125067f));
        latLngs.add(new LatLng(22.56257168253623f, 113.89786968614327f));
        latLngs.add(new LatLng(22.56333815070074f, 113.89689412668393f));
        latLngs.add(new LatLng(22.563350985087038f, 113.89687761139977f));
        latLngs.add(new LatLng(22.56381957466948f, 113.89622286003495f));
        latLngs.add(new LatLng(22.563811017707557f, 113.89622503240996f));
        latLngs.add(new LatLng(22.565052407368444f, 113.89444953270996f));
        latLngs.add(new LatLng(22.564813404815382f, 113.89479947547262f));
        latLngs.add(new LatLng(22.567064353799395f, 113.89280285979696f));
        latLngs.add(new LatLng(22.566863787439356f, 113.89296245410772f));
        latLngs.add(new LatLng(22.56862905589468f, 113.89163128041433f));
        latLngs.add(new LatLng(22.56831739267901f, 113.89185380395931f));
        latLngs.add(new LatLng(22.569825394595675f, 113.89034357407915f));
        latLngs.add(new LatLng(22.56982497594054f, 113.89034284291856f));
        latLngs.add(new LatLng(22.570106788484203f, 113.89009881180193f));
        latLngs.add(new LatLng(22.570480826819193f, 113.88981961722013f));
        latLngs.add(new LatLng(22.570424775489176f, 113.88985679019552f));
        latLngs.add(new LatLng(22.571197099463003f, 113.8890479251769f));
        latLngs.add(new LatLng(22.571196850423537f, 113.8890481419546f));
        latLngs.add(new LatLng(22.572287997659128f, 113.88770313868237f));
        latLngs.add(new LatLng(22.572300220762358f, 113.88769160211815f));
        latLngs.add(new LatLng(22.573153154153598f, 113.88636121513768f));
        latLngs.add(new LatLng(22.57315513739185f, 113.88634429646567f));
        latLngs.add(new LatLng(22.574807270030636f, 113.88540309646746f));
        latLngs.add(new LatLng(22.574725985551243f, 113.88543978040498f));
        latLngs.add(new LatLng(22.57524877803594f, 113.88482775498096f));
        latLngs.add(new LatLng(22.575169930158168f, 113.8849115475509f));
        latLngs.add(new LatLng(22.576398410847037f, 113.88334780548838f));
        latLngs.add(new LatLng(22.57639272970402f, 113.88335206447681f));
        latLngs.add(new LatLng(22.57714959448045f, 113.88250461285558f));
        latLngs.add(new LatLng(22.5769649336007f, 113.88270903835979f));
        latLngs.add(new LatLng(22.57823248426166f, 113.88121688538791f));
        latLngs.add(new LatLng(22.57799317359725f, 113.88152988541448f));
        latLngs.add(new LatLng(22.579219911661177f, 113.88048560405466f));
        latLngs.add(new LatLng(22.579123908008402f, 113.8805725169729f));
        latLngs.add(new LatLng(22.579532884616174f, 113.88009434571005f));
        latLngs.add(new LatLng(22.579532146152697f, 113.88009868414504f));
        latLngs.add(new LatLng(22.57953726129295f, 113.88009433292686f));
        latLngs.add(new LatLng(22.58033466674211f, 113.87933702591421f));
        latLngs.add(new LatLng(22.580343763620288f, 113.87932807080989f));
        latLngs.add(new LatLng(22.581454973177678f, 113.87810800190698f));
        latLngs.add(new LatLng(22.581458075881585f, 113.87809480958215f));
        latLngs.add(new LatLng(22.582482829073097f, 113.87705527301739f));
        latLngs.add(new LatLng(22.582432769333256f, 113.87709533048267f));
        latLngs.add(new LatLng(22.58270133276988f, 113.87684479273823f));
        latLngs.add(new LatLng(22.582696480236823f, 113.87684718006064f));
        latLngs.add(new LatLng(22.584467365482354f, 113.87532643538447f));
        latLngs.add(new LatLng(22.584018457020118f, 113.87568941675916f));
        latLngs.add(new LatLng(22.586073129931027f, 113.87385729784992f));
        latLngs.add(new LatLng(22.585488339885046f, 113.8743558687473f));
        latLngs.add(new LatLng(22.586156280375768f, 113.8731106982561f));
        latLngs.add(new LatLng(22.58601887141675f, 113.87336357070996f));
        latLngs.add(new LatLng(22.587340467002125f, 113.87300176522679f));
        latLngs.add(new LatLng(22.58689634103464f, 113.87312672298626f));
        latLngs.add(new LatLng(22.587278788293425f, 113.87266835877935f));
        latLngs.add(new LatLng(22.58751287579877f, 113.87242453578217f));
        latLngs.add(new LatLng(22.587511370362563f, 113.87242527033956f));
        latLngs.add(new LatLng(22.588901407303474f, 113.87194446445167f));
        latLngs.add(new LatLng(22.588850441059073f, 113.87195696119639f));
        latLngs.add(new LatLng(22.58962245475294f, 113.87016081585682f));
        latLngs.add(new LatLng(22.589629561126355f, 113.87013483485244f));
        latLngs.add(new LatLng(22.591614260141288f, 113.8695341176048f));
        latLngs.add(new LatLng(22.591590858364665f, 113.86953695134437f));
        latLngs.add(new LatLng(22.592871467962286f, 113.868684755239f));
        latLngs.add(new LatLng(22.592854573566004f, 113.86868632028164f));
        latLngs.add(new LatLng(22.593967020890766f, 113.86816924908604f));
        latLngs.add(new LatLng(22.593938875704584f, 113.86817430610142f));
        latLngs.add(new LatLng(22.59520813200391f, 113.86692824707141f));
        latLngs.add(new LatLng(22.59505481022522f, 113.86705530894002f));
        latLngs.add(new LatLng(22.59505469692369f, 113.86705606523746f));
        latLngs.add(new LatLng(22.595055227811088f, 113.8670560807617f));
        latLngs.add(new LatLng(22.595964234109136f, 113.86649984734575f));
        latLngs.add(new LatLng(22.59594138744958f, 113.86651203894118f));
        latLngs.add(new LatLng(22.5971134252212f, 113.86556229341527f));
        latLngs.add(new LatLng(22.596980437231185f, 113.86566767537411f));
        latLngs.add(new LatLng(22.5988901840273f, 113.86506157111343f));
        latLngs.add(new LatLng(22.598670028147332f, 113.86514058146169f));
        latLngs.add(new LatLng(22.598587680782206f, 113.86460988703061f));
        latLngs.add(new LatLng(22.59859826561791f, 113.8646791760143f));
        latLngs.add(new LatLng(22.59859783040406f, 113.86467721122308f));
        latLngs.add(new LatLng(22.59859780747049f, 113.86467725012538f));
        latLngs.add(new LatLng(22.598597783041253f, 113.86467729156479f));
        latLngs.add(new LatLng(22.60191602443125f, 113.86304097333985f));
        latLngs.add(new LatLng(22.601897984004445f, 113.86304671874358f));
        latLngs.add(new LatLng(22.601901886141643f, 113.86304550612486f));
        latLngs.add(new LatLng(22.60190137765339f, 113.86304571019029f));
        latLngs.add(new LatLng(22.60190129171961f, 113.86304571052499f));
        latLngs.add(new LatLng(22.61719280215716f, 113.85251198520355f));
        latLngs.add(new LatLng(22.615592409842233f, 113.85353047893103f));
        latLngs.add(new LatLng(22.61561450942365f, 113.85351458580188f));
        latLngs.add(new LatLng(22.615616239175203f, 113.85351670821089f));
        latLngs.add(new LatLng(22.615624330985078f, 113.85351477934843f));
        latLngs.add(new LatLng(22.615632422794953f, 113.85351285048597f));
        latLngs.add(new LatLng(22.615798501849618f, 113.85211669248645f));
        latLngs.add(new LatLng(22.615787084997198f, 113.85219585664991f));
        latLngs.add(new LatLng(22.617322701902488f, 113.8519411006171f));
        latLngs.add(new LatLng(22.617342101450745f, 113.85193802264008f));
        latLngs.add(new LatLng(22.618495644656324f, 113.85213557192999f));
        latLngs.add(new LatLng(22.61847854755895f, 113.85213445922511f));
        latLngs.add(new LatLng(22.618517003097306f, 113.85262677322969f));
        latLngs.add(new LatLng(22.618513684073356f, 113.85258997871378f));
        latLngs.add(new LatLng(22.61862558414816f, 113.85263225011225f));
        latLngs.add(new LatLng(22.618616994842682f, 113.85262957398194f));
        latLngs.add(new LatLng(22.618616989043485f, 113.85262956656015f));
        latLngs.add(new LatLng(22.618616993485713f, 113.8526295677444f));
        latLngs.add(new LatLng(22.618821703984736f, 113.85258117406165f));
        latLngs.add(new LatLng(22.6188057798569f, 113.8525863361675f));
        latLngs.add(new LatLng(22.61884241442588f, 113.85305134763361f));
        latLngs.add(new LatLng(22.618833437500616f, 113.8529433184227f));
        latLngs.add(new LatLng(22.61883344380027f, 113.85294328374692f));
        latLngs.add(new LatLng(22.61898599646501f, 113.85290009768178f));
        latLngs.add(new LatLng(22.618989382476162f, 113.85289917254869f));
        latLngs.add(new LatLng(22.619131373664054f, 113.85333661861911f));
        latLngs.add(new LatLng(22.619132000716558f, 113.85333817169617f));
        latLngs.add(new LatLng(22.619295985156867f, 113.85351754866048f));
        latLngs.add(new LatLng(22.619296254245143f, 113.85351760258898f));
        latLngs.add(new LatLng(22.61938685475792f, 113.85374437741898f));
        latLngs.add(new LatLng(22.619380255193136f, 113.85373072708028f));
        latLngs.add(new LatLng(22.61938056719045f, 113.85373108860831f));
        latLngs.add(new LatLng(22.619380567213664f, 113.85373109398277f));
        latLngs.add(new LatLng(22.62083599566218f, 113.8549032414108f));
        latLngs.add(new LatLng(22.62035479921784f, 113.85451615160893f));
        latLngs.add(new LatLng(22.619950770704065f, 113.85419438252791f));
        latLngs.add(new LatLng(22.61997621811345f, 113.85422219439648f));
        latLngs.add(new LatLng(22.619978510600692f, 113.85422344958519f));
        latLngs.add(new LatLng(22.619976058037313f, 113.85422210151782f));
        latLngs.add(new LatLng(22.62003241848364f, 113.85436051038833f));
        latLngs.add(new LatLng(22.62003217614951f, 113.85436008980903f));
        latLngs.add(new LatLng(22.620173578974732f, 113.85448015032637f));
        latLngs.add(new LatLng(22.62014293641227f, 113.85445420004716f));
        latLngs.add(new LatLng(22.6201436645344f, 113.85445481282957f));
        latLngs.add(new LatLng(22.621640717094877f, 113.85616425511934f));
        latLngs.add(new LatLng(22.62160581044778f, 113.85613647068318f));
        latLngs.add(new LatLng(22.621899644164014f, 113.85632524590305f));
        latLngs.add(new LatLng(22.62181540575769f, 113.8562709755017f));
        latLngs.add(new LatLng(22.622278798117858f, 113.8562035797191f));
        latLngs.add(new LatLng(22.622274954142103f, 113.85620400432406f));
        latLngs.add(new LatLng(22.62227879486083f, 113.85620358140073f));
        latLngs.add(new LatLng(22.62247205632543f, 113.85624424839429f));
        latLngs.add(new LatLng(22.62235886955551f, 113.8568945931298f));
        latLngs.add(new LatLng(22.62245949148363f, 113.85679324225168f));
        latLngs.add(new LatLng(22.622499468538237f, 113.85689142011461f));
        latLngs.add(new LatLng(22.62287950134385f, 113.85695540327558f));
        latLngs.add(new LatLng(22.622808702444484f, 113.8569261847467f));
        latLngs.add(new LatLng(22.622671570497165f, 113.8568763070884f));
        latLngs.add(new LatLng(22.623702557806958f, 113.85688602657736f));
        latLngs.add(new LatLng(22.623112747570136f, 113.85636967334074f));
        latLngs.add(new LatLng(22.623063526599893f, 113.85648134032024f));
        latLngs.add(new LatLng(22.622931030309378f, 113.85712360797167f));
        latLngs.add(new LatLng(22.622932350941483f, 113.8571187388918f));
        latLngs.add(new LatLng(22.623350961017902f, 113.85653336415635f));
        latLngs.add(new LatLng(22.623287289650605f, 113.85661735783812f));
        latLngs.add(new LatLng(22.623573354353738f, 113.85746253565613f));
        latLngs.add(new LatLng(22.623531258182144f, 113.85731208066665f));
        latLngs.add(new LatLng(22.623562569546618f, 113.85687801750977f));
        latLngs.add(new LatLng(22.623562354071534f, 113.85688052236182f));
        latLngs.add(new LatLng(22.623753136993702f, 113.85735106111541f));
        latLngs.add(new LatLng(22.623753318585027f, 113.8573477754278f));
        latLngs.add(new LatLng(22.623755214623607f, 113.85735105253566f));
        latLngs.add(new LatLng(22.623755221068784f, 113.85735106374186f));
        latLngs.add(new LatLng(22.62375522108108f, 113.8573510637725f));
        latLngs.add(new LatLng(22.623767414259035f, 113.85685974405212f));
        latLngs.add(new LatLng(22.623766520056297f, 113.85689095542445f));
        latLngs.add(new LatLng(22.62376658651047f, 113.85688899411902f));
        latLngs.add(new LatLng(22.626765186035737f, 113.85864599530245f));
        latLngs.add(new LatLng(22.626317779104152f, 113.85837474895958f));
        latLngs.add(new LatLng(22.62630703856727f, 113.85839859144302f));
        latLngs.add(new LatLng(22.62774902467141f, 113.86048481167057f));
        latLngs.add(new LatLng(22.627748925738626f, 113.86048466649201f));
        latLngs.add(new LatLng(22.627748924827706f, 113.86048466574962f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.627748924827287f, 113.86048466574972f));
        latLngs.add(new LatLng(22.6278579215632f, 113.86052072563676f));
        latLngs.add(new LatLng(22.627857954687176f, 113.86052073603503f));
        latLngs.add(new LatLng(22.62785795495346f, 113.86052073610865f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.62785795495362f, 113.86052073610867f));
        latLngs.add(new LatLng(22.627831804831068f, 113.86039852105566f));
        latLngs.add(new LatLng(22.62783179364241f, 113.86039847565394f));
        latLngs.add(new LatLng(22.62783179363693f, 113.86039847564523f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62783179363677f, 113.86039847564449f));
        latLngs.add(new LatLng(22.62781170171533f, 113.86035230272346f));
        latLngs.add(new LatLng(22.62781173445896f, 113.8603523779727f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.627811734459037f, 113.86035237797276f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.627811026637726f, 113.86034431137794f));
        latLngs.add(new LatLng(22.6278107238495f, 113.86034436081418f));
        latLngs.add(new LatLng(22.627810723734886f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.62781072373482f, 113.86034436082218f));
        latLngs.add(new LatLng(22.627810705996374f, 113.86033131208917f));
        latLngs.add(new LatLng(22.627810706024597f, 113.86033133284495f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627810706024597f, 113.860331332845f));
        latLngs.add(new LatLng(22.627816735037754f, 113.86035338004965f));
        latLngs.add(new LatLng(22.62781673496071f, 113.86035337980601f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.62781673496067f, 113.86035337980593f));
        latLngs.add(new LatLng(22.627810697120122f, 113.86033730026993f));
        latLngs.add(new LatLng(22.627810714198844f, 113.86033734575767f));
        latLngs.add(new LatLng(22.62781071419913f, 113.86033734575817f));
        latLngs.add(new LatLng(22.62781071419913f, 113.86033734575817f));
        latLngs.add(new LatLng(22.62781071419913f, 113.86033734575817f));
        latLngs.add(new LatLng(22.62781071419913f, 113.86033734575817f));
        latLngs.add(new LatLng(22.62781071419913f, 113.86033734575817f));
        latLngs.add(new LatLng(22.627810701341048f, 113.86032787794421f));
        latLngs.add(new LatLng(22.627810663853584f, 113.86029984646316f));
        latLngs.add(new LatLng(22.627810627023795f, 113.86027230676248f));
        latLngs.add(new LatLng(22.627810590194006f, 113.8602447670618f));
        latLngs.add(new LatLng(22.627810553364217f, 113.86021722736112f));
        latLngs.add(new LatLng(22.627810516534428f, 113.86018968766044f));
        latLngs.add(new LatLng(22.627810479046964f, 113.86016165617939f));
        latLngs.add(new LatLng(22.627810452082297f, 113.86014149318424f));
        latLngs.add(new LatLng(22.627810415252508f, 113.86011395348356f));
        latLngs.add(new LatLng(22.62781037842272f, 113.86008641378288f));
        latLngs.add(new LatLng(22.62781034159293f, 113.8600588740822f));
        latLngs.add(new LatLng(22.62781030476314f, 113.86003133438152f));
        latLngs.add(new LatLng(22.627810267933352f, 113.86000379468084f));
        latLngs.add(new LatLng(22.627810231103563f, 113.85997625498015f));
        latLngs.add(new LatLng(22.6278101936161f, 113.8599482234991f));
        latLngs.add(new LatLng(22.627810719648263f, 113.86034135436635f));
        latLngs.add(new LatLng(22.62781062850893f, 113.86027354108533f));
        latLngs.add(new LatLng(22.627810557092733f, 113.86022030413754f));
        latLngs.add(new LatLng(22.62781975895409f, 113.86037141833955f));
        latLngs.add(new LatLng(22.62781975895409f, 113.86037141833955f));
        latLngs.add(new LatLng(22.627808175952953f, 113.86031995051651f));
        latLngs.add(new LatLng(22.627756773684883f, 113.86008942781639f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627811720838423f, 113.86034235645462f));
        latLngs.add(new LatLng(22.627810263515638f, 113.8603350683814f));
        latLngs.add(new LatLng(22.627804986649178f, 113.86030845024851f));
        latLngs.add(new LatLng(22.62779961555296f, 113.86028135679182f));
        latLngs.add(new LatLng(22.627810786364066f, 113.86039045977859f));
        latLngs.add(new LatLng(22.627810786364066f, 113.86039045977859f));
        latLngs.add(new LatLng(22.627810786364066f, 113.86039045977859f));
        latLngs.add(new LatLng(22.627697438836154f, 113.8605039059899f));
        latLngs.add(new LatLng(22.627661308507747f, 113.86047150767011f));
        latLngs.add(new LatLng(22.62765935535718f, 113.86046641746277f));
        latLngs.add(new LatLng(22.627659304089235f, 113.86046634190632f));
        latLngs.add(new LatLng(22.627659304082783f, 113.86046634191038f));
        latLngs.add(new LatLng(22.627659304082812f, 113.86046634191038f));
        latLngs.add(new LatLng(22.62765930408281f, 113.86046634191038f));
        latLngs.add(new LatLng(22.62765930408281f, 113.86046634191038f));
        latLngs.add(new LatLng(22.627659304082805f, 113.86046634191038f));
        latLngs.add(new LatLng(22.627659304082805f, 113.86046634191038f));
        latLngs.add(new LatLng(22.627659304082805f, 113.86046634191038f));
        latLngs.add(new LatLng(22.627669841036262f, 113.85974817473185f));
        latLngs.add(new LatLng(22.627502003487013f, 113.86040754237281f));
        latLngs.add(new LatLng(22.627403205311563f, 113.86036248498216f));
        latLngs.add(new LatLng(22.627406999802034f, 113.86036375910217f));
        latLngs.add(new LatLng(22.627406999570315f, 113.86036375897162f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627406999569956f, 113.86036375897164f));
        latLngs.add(new LatLng(22.627810708728987f, 113.8603333371389f));
        latLngs.add(new LatLng(22.62781070874955f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));
        latLngs.add(new LatLng(22.627810708749553f, 113.8603333371495f));


        return latLngs;
    }

    public static long[] latLngsTimes = {1586562124, 1586562135, 1586562145, 1586562154, 1586562163, 1586562173, 1586562182, 1586562192, 1586562201, 1586562210, 1586562220, 1586562229, 1586562239, 1586562248, 1586562257, 1586562267, 1586562277, 1586562286, 1586562295, 1586562305, 1586562314, 1586562324, 1586562333, 1586562342, 1586562352, 1586562361, 1586562371, 1586562380, 1586562389, 1586562399, 1586562408, 1586562418, 1586562427, 1586562437, 1586562447, 1586562458, 1586562469, 1586562478, 1586562492, 1586562501, 1586562511, 1586562520, 1586562530, 1586562539, 1586562550, 1586562559, 1586562568, 1586562578, 1586562588, 1586562598, 1586562607, 1586562617, 1586562626, 1586562636, 1586562645, 1586562655, 1586562664, 1586562674, 1586562683, 1586562693, 1586562702, 1586562711, 1586562723, 1586562733, 1586562743, 1586562752, 1586562761, 1586562772, 1586562781, 1586562791, 1586562800, 1586562811, 1586562821, 1586562830, 1586562840, 1586562849, 1586562859, 1586562868, 1586562877, 1586562888, 1586562898, 1586562907, 1586562917, 1586562926, 1586562936, 1586562945, 1586562956, 1586562965, 1586562975, 1586562984, 1586562993, 1586563003, 1586563013, 1586563023, 1586563032, 1586563042, 1586563051, 1586563061, 1586563070, 1586563080, 1586563089, 1586563098, 1586563108, 1586563117, 1586563127, 1586563136, 1586563145, 1586563155, 1586563164, 1586563174, 1586563183, 1586563193, 1586563202, 1586563213, 1586563222, 1586563231, 1586563241, 1586563250, 1586563260, 1586563269, 1586563279, 1586563291, 1586563300, 1586563309, 1586563319, 1586563328, 1586563338, 1586563347, 1586563357, 1586563366, 1586563376, 1586563385, 1586563394, 1586563404, 1586563413, 1586563423, 1586563432, 1586563441, 1586563451, 1586563460, 1586563470, 1586563479, 1586563488, 1586563498, 1586563507, 1586563517, 1586563526, 1586563535, 1586563545, 1586563554, 1586563563, 1586563574, 1586563584, 1586563594, 1586563604, 1586563613, 1586563622, 1586563632, 1586563641, 1586563651, 1586563660, 1586563669, 1586563679, 1586563688, 1586563697, 1586563707, 1586563716, 1586563726, 1586563735, 1586563745, 1586563754, 1586563763, 1586563773, 1586563782, 1586563791, 1586563801, 1586563811, 1586563820, 1586563830, 1586563839, 1586563849, 1586563858, 1586563867, 1586563878, 1586563888, 1586563897, 1586563907, 1586563916, 1586563926, 1586563935, 1586563946, 1586563955, 1586563965, 1586563975, 1586563984, 1586563994, 1586564003, 1586564013, 1586564025, 1586564035, 1586564045, 1586564054, 1586564063, 1586564073, 1586564082, 1586564092, 1586564101, 1586564111, 1586564120, 1586564129, 1586564139, 1586564148, 1586564159, 1586564168, 1586564178, 1586564187, 1586564196, 1586564206, 1586564215, 1586564226, 1586564235, 1586564245, 1586564254, 1586564263, 1586564273, 1586564282, 1586564292, 1586564301, 1586564311, 1586564321, 1586564331, 1586564340, 1586564349, 1586564359, 1586564368, 1586564378, 1586564387, 1586564396, 1586564406, 1586564415, 1586564425, 1586564434, 1586564443, 1586564453, 1586564462, 1586564472, 1586564482, 1586564492, 1586564501, 1586564511, 1586564520, 1586564529, 1586564539, 1586564548, 1586564558, 1586564567, 1586564576, 1586564586, 1586564595, 1586564606, 1586564615, 1586564625, 1586564634, 1586564644, 1586564653, 1586564663, 1586564672, 1586564682, 1586564691, 1586564700, 1586564710, 1586564719, 1586564729, 1586564738, 1586564748, 1586564757, 1586564768, 1586564777, 1586564788, 1586564797, 1586564807, 1586564816, 1586564825, 1586564835, 1586564844, 1586564854, 1586564863, 1586564873, 1586564883, 1586564893, 1586564902, 1586564912, 1586564921, 1586564931, 1586564988, 1586565063, 1586565099, 1586565177, 1586565237, 1586565289, 1586565368, 1586565377, 1586565387, 1586565396, 1586565407, 1586565416, 1586565426, 1586565435, 1586565445, 1586565454, 1586565464, 1586565473, 1586565483, 1586565492, 1586565502, 1586565511, 1586565520, 1586565530, 1586565539, 1586565549, 1586565558, 1586565567, 1586565577, 1586565586, 1586565596, 1586565605, 1586565615, 1586565624, 1586565634, 1586565643, 1586565652, 1586565662, 1586565671, 1586565681, 1586565690, 1586565699, 1586565709, 1586565718, 1586565737, 1586565844, 1586565886, 1586565899, 1586565908, 1586565918, 1586565927, 1586565937, 1586565946, 1586565955, 1586565965, 1586565974, 1586565984, 1586565993, 1586566002, 1586566012, 1586566021, 1586566030, 1586566040, 1586566049, 1586566059, 1586566068, 1586566077, 1586566087, 1586566096, 1586566106, 1586566115, 1586566124, 1586566134, 1586566145, 1586566154, 1586566164, 1586566173, 1586566195, 1586566254, 1586566321, 1586566397, 1586566544, 1586566601, 1586566610, 1586566619, 1586566629, 1586566638, 1586566648, 1586566657, 1586566666, 1586566673, 1586566684, 1586566694, 1586566703, 1586566712, 1586566722, 1586566731, 1586566740, 1586566750, 1586566775, 1586566785, 1586566794, 1586566804, 1586566813, 1586566824, 1586566833, 1586566880, 1586566893, 1586566902, 1586566911, 1586566928, 1586566938, 1586566947, 1586566957, 1586566966, 1586566975, 1586566985, 1586566994, 1586567003, 1586567013, 1586567022, 1586567031, 1586567041, 1586567050, 1586567059, 1586567069, 1586567078, 1586567087, 1586567097, 1586567106, 1586567115, 1586567125, 1586567134, 1586567143, 1586567153, 1586567162, 1586567173, 1586567182, 1586567191, 1586567201, 1586567211, 1586567221, 1586567231, 1586567241, 1586567250, 1586567259, 1586567269, 1586567278, 1586567287, 1586567297, 1586567307, 1586567316, 1586567326, 1586567336, 1586567347, 1586567357, 1586567368, 1586567377, 1586567388, 1586567399, 1586567408, 1586567418, 1586567428, 1586567437, 1586567448, 1586567458, 1586567468, 1586567478, 1586567489, 1586567499, 1586567509, 1586567519, 1586567528, 1586567538, 1586567547, 1586567557, 1586567566, 1586567575, 1586567585, 1586567594, 1586567603, 1586567613, 1586567623, 1586567633, 1586567642, 1586567651, 1586567661, 1586567670, 1586567679, 1586567690, 1586567699, 1586567708, 1586567718, 1586567727, 1586567738, 1586567747, 1586567756, 1586567766, 1586567776, 1586567786, 1586567795, 1586567804, 1586567814, 1586567823, 1586567832, 1586567843, 1586567852, 1586567862, 1586567871, 1586567880, 1586567890, 1586567899, 1586567909, 1586567918, 1586567928, 1586567938, 1586567947, 1586567957, 1586567966, 1586567975, 1586567985, 1586567994, 1586568003, 1586568013, 1586568022, 1586568033, 1586568042, 1586568051, 1586568062, 1586568071, 1586568080, 1586568090, 1586568099, 1586568110, 1586568120, 1586568130, 1586568139, 1586568148, 1586568159, 1586568168, 1586568178, 1586568187, 1586568196, 1586568206, 1586568215, 1586568224, 1586568235, 1586568245, 1586568255, 1586568264, 1586568273, 1586568283, 1586568292, 1586568303, 1586568312, 1586568321, 1586568331, 1586568340, 1586568349, 1586568359, 1586568368, 1586568378, 1586568387, 1586568396, 1586568407, 1586568416, 1586568426, 1586568435, 1586568444, 1586568454, 1586568463, 1586568472, 1586568482, 1586568491, 1586568500, 1586568510, 1586568519, 1586568528, 1586568538, 1586568547, 1586568556, 1586568566, 1586568575, 1586568584, 1586568594, 1586568603, 1586568612, 1586568622, 1586568631, 1586568640, 1586568650, 1586568659, 1586568670, 1586568679, 1586568688, 1586568698, 1586568707, 1586568716, 1586568726, 1586568735, 1586568746, 1586568755, 1586568764, 1586568775, 1586568784, 1586568793, 1586568803, 1586568812, 1586568822, 1586568831, 1586568840, 1586568850, 1586568859, 1586568869, 1586568878, 1586568887, 1586568897, 1586568906, 1586568915, 1586568926, 1586568935, 1586568945, 1586568954, 1586568963, 1586568973, 1586568983, 1586568992, 1586569003, 1586569012, 1586569022, 1586569031, 1586569040, 1586569050, 1586569059, 1586569069, 1586569078, 1586569087, 1586569097, 1586569106, 1586569115, 1586569125, 1586569134, 1586569143, 1586569154, 1586569163, 1586569173, 1586569183, 1586569192, 1586569202, 1586569212, 1586569223, 1586569233, 1586569243, 1586569252, 1586569262, 1586569271, 1586569280, 1586569290, 1586569299, 1586569308, 1586569318, 1586569328, 1586569339, 1586569349, 1586569360, 1586569369, 1586569378, 1586569388, 1586569397, 1586569406, 1586569416, 1586569425, 1586569434, 1586569444, 1586569453, 1586569465, 1586569474, 1586569483, 1586569493, 1586569502, 1586569513, 1586569522, 1586569531, 1586569542, 1586569551, 1586569562, 1586569571, 1586569580, 1586569590, 1586569599, 1586569611, 1586569621, 1586569631, 1586569640, 1586569651, 1586569660, 1586569670, 1586569680, 1586569689, 1586569698, 1586569708, 1586569717, 1586569727, 1586569736, 1586569746, 1586569755, 1586569764, 1586569774, 1586569784, 1586569793, 1586569804, 1586569813, 1586569824, 1586569833, 1586569843, 1586569852, 1586569861, 1586569871, 1586569880, 1586569889, 1586569899, 1586569908, 1586569917, 1586569927, 1586569937, 1586569947, 1586569956, 1586569965, 1586569975, 1586569984, 1586569993, 1586570003, 1586570012, 1586570021, 1586570031, 1586570041, 1586570052, 1586570062, 1586570072, 1586570081, 1586570090, 1586570101, 1586570110, 1586570120, 1586570129, 1586570138, 1586570148, 1586570157, 1586570166, 1586570176, 1586570186, 1586570196, 1586570206, 1586570218, 1586570228, 1586570239, 1586570248, 1586570258, 1586570267, 1586570276, 1586570286, 1586570295, 1586570304, 1586570314, 1586570324, 1586570333, 1586570343, 1586570353, 1586570364, 1586570373, 1586570384, 1586570394, 1586570404, 1586570413, 1586570423, 1586570433, 1586570442, 1586570452, 1586570461, 1586570471, 1586570482, 1586570491, 1586570502, 1586570512, 1586570522, 1586570531, 1586570540, 1586570550, 1586570559, 1586570569, 1586570578, 1586570587, 1586570597, 1586570606, 1586570615, 1586570625, 1586570634, 1586570643, 1586570653, 1586570662, 1586570671, 1586570681, 1586570691, 1586570700, 1586570710, 1586570719, 1586570730, 1586570739, 1586570748, 1586570759, 1586570768, 1586570778, 1586570787, 1586570796, 1586570806, 1586570815, 1586570824, 1586570835, 1586570845, 1586570855, 1586570865, 1586570875, 1586570884, 1586570893, 1586570903, 1586570912, 1586570921, 1586570931, 1586570940, 1586570949, 1586570960, 1586570970, 1586570981, 1586570990, 1586571000, 1586571009, 1586571018, 1586571029, 1586571038, 1586571049, 1586571058, 1586571069, 1586571078, 1586571087, 1586571097, 1586571106, 1586571116, 1586571127, 1586571136, 1586571147, 1586571156, 1586571166, 1586571175, 1586571184, 1586571195, 1586571205, 1586571215, 1586571224, 1586571233, 1586571243, 1586571252, 1586571261, 1586571271, 1586571281, 1586571290, 1586571302, 1586571313, 1586571323, 1586571333, 1586571342, 1586571351, 1586571362, 1586571374, 1586571384, 1586571393, 1586571403, 1586571412, 1586571422, 1586571433, 1586571443, 1586571452, 1586571463, 1586571472, 1586571481, 1586571491, 1586571500, 1586571513, 1586571523, 1586571533, 1586571544, 1586571555, 1586571570, 1586571580, 1586571592, 1586571603, 1586571615, 1586571626, 1586571637, 1586571644, 1586571657, 1586571668, 1586571679, 1586571691, 1586571702, 1586571714, 1586571725, 1586571834, 1586571845, 1586571858, 1586572012, 1586572023, 1586572107, 1586572118, 1586572366, 1586572377, 1586572389, 1586572400, 1586572411, 1586572424, 1586572463, 1586572475, 1586572486, 1586572517, 1586572528, 1586572540, 1586572551, 1586572565, 1586572576, 1586572587, 1586572600, 1586572611, 1586572623, 1586572634, 1586572646, 1586572657, 1586572668, 1586572698, 1586572709, 1586572722, 1586572733, 1586572744, 1586572756, 1586572767, 1586572779, 1586572790, 1586572801, 1586572811, 1586572820, 1586572829, 1586572839, 1586572848, 1586572858, 1586572867, 1586572877, 1586572886, 1586572895, 1586572905, 1586572914, 1586572925, 1586572935, 1586572944, 1586572954, 1586572963, 1586572973, 1586572982, 1586572991, 1586573002, 1586573011, 1586573021, 1586573033, 1586573043, 1586573052, 1586573061, 1586573071, 1586573080, 1586573090, 1586573099, 1586573110, 1586573119, 1586573128, 1586573138, 1586573147, 1586573156, 1586573166, 1586573175, 1586573186, 1586573195, 1586573204, 1586573214, 1586573223, 1586573232, 1586573242, 1586573251, 1586573260, 1586573270, 1586573279, 1586573288, 1586573298, 1586573307, 1586573317, 1586573326, 1586573335, 1586573345, 1586573355, 1586573364, 1586573374, 1586573383, 1586573393, 1586573402, 1586573411, 1586573421, 1586573430, 1586573439, 1586573449, 1586573458, 1586573467, 1586573477, 1586573486, 1586573495, 1586573505, 1586573514, 1586573523, 1586573533, 1586573542, 1586573552, 1586573561, 1586573570, 1586573581, 1586573590, 1586573600, 1586573609, 1586573619, 1586573628, 1586573637, 1586573647, 1586573656, 1586573665, 1586573675, 1586573684, 1586573693, 1586573703, 1586573712, 1586573721, 1586573731, 1586573762, 1586573773, 1586573785, 1586573796, 1586573808, 1586573819, 1586573830, 1586573842, 1586573853, 1586573864, 1586573876, 1586573887, 1586573899, 1586573910, 1586573921, 1586573933, 1586573944, 1586573956, 1586573967, 1586573978, 1586573990, 1586574001, 1586574013, 1586574024, 1586574035, 1586574047, 1586574058, 1586574069, 1586574081, 1586574092, 1586574103};
}
