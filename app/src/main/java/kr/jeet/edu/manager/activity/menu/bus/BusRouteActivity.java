package kr.jeet.edu.manager.activity.menu.bus;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.BusRouteListAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.BusInfoData;
import kr.jeet.edu.manager.model.data.BusRouteData;
import kr.jeet.edu.manager.model.response.BusRouteResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.ProgressFloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusRouteActivity extends BaseActivity {
    private static final String TAG = "bus_route";
    private static final int REFRESH_SEC = 30;
    private static final int CMD_REFRESH = 0;
    private RecyclerView mRecyclerRoute;
    private TextView mTvListEmpty, mTvBcName, _tvBusName, _tvPhoneNumber;
    private ProgressFloatingActionButton _fab;
    private FloatingActionButton fabBtn;
    private ProgressBar _fabProgres;
    ImageButton _btnDriverCall;
    ObjectAnimator animation;
    int _positionBus = 0;
    BusInfoData _currentData = null;
    String bcName = "";

    private BusRouteListAdapter mAdapter;
    private ArrayList<BusRouteData> mList = new ArrayList<>();
    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what) {
                case CMD_REFRESH:
                    requestRouteList();

                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);
        mContext = this;
//        _timer = new CountDownTimer(REFRESH_SEC * 1000, 1000) {
//            @Override
//            public void onTick(long left) {
//                int progress = _fabProgres.getMax() / REFRESH_SEC;
//
//                _fabProgres.setProgress((int)left / 1000 * progress);
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        };
        initIntentData();
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }
    void initIntentData() {
        Intent intent = getIntent();
        if(intent == null) return;
        if(intent.hasExtra(IntentParams.PARAM_BOARD_ITEM)) {
            LogMgr.w("param is recived");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM, BusInfoData.class);
            }else{
                _currentData = intent.getParcelableExtra(IntentParams.PARAM_BOARD_ITEM);
            }
        }
        if(intent.hasExtra(IntentParams.PARAM_BCNAME)) {
            bcName = intent.getStringExtra(IntentParams.PARAM_BCNAME);
        }
    }
    void initView() {
        mRecyclerRoute = findViewById(R.id.recycler_bus_route);
        mTvListEmpty = findViewById(R.id.tv_empty_list);
        mTvBcName = findViewById(R.id.tv_bc_name);
        _tvBusName = findViewById(R.id.tv_bus_name);
        _tvPhoneNumber = findViewById(R.id.tv_bus_phonenumber);
        _btnDriverCall = findViewById(R.id.btn_call);

        _fab = findViewById(R.id.progress_fab);
        _fab.setOnClickListener(this);
        fabBtn = findViewById(R.id.fab);
        _fabProgres = findViewById(R.id.fab_progress);
        _fabProgres.setIndeterminate(false);
        setRecycler();
        if(_currentData != null) {
            _tvBusName.setText(_currentData.busName);
            _tvPhoneNumber.setText(Utils.formatPhoneNumber(_currentData.busPhoneNumber));
            if(Utils.checkPhoneNumber(_currentData.busPhoneNumber.replace("-", ""))) {
                _btnDriverCall.setOnClickListener(this);
            }else{
                _btnDriverCall.setEnabled(false);
            }
        }else{
            Toast.makeText(mContext, R.string.empty_list_bus_route, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setRecycler(){
        mAdapter = new BusRouteListAdapter(mContext, mList, new BusRouteListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(ArrayList<BusRouteData> item, int position) {

            }
        });
        mRecyclerRoute.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(_currentData != null) {
            requestRouteList();
        }
    }

    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_bus_route_info);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.progress_fab:
                if(animation != null && animation.isRunning()) animation.end();
//                _timer.cancel();
                _handler.removeMessages(CMD_REFRESH);
                requestRouteList();
                break;
            case R.id.btn_call:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + _currentData.busPhoneNumber.replace("-", "")));
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onPause() {
        if(animation != null && animation.isRunning()) animation.end();
//        _timer.cancel();
        _handler.removeMessages(CMD_REFRESH);
        super.onPause();
    }

    // 버스 노선조회
    private void requestRouteList(){
        doFABAnimation();
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusRoute(bcName, _currentData.busCode).enqueue(new Callback<BusRouteResponse>() {
                @Override
                public void onResponse(Call<BusRouteResponse> call, Response<BusRouteResponse> response) {
                    boolean isScrollY = false;
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            if (mList!=null && mList.size() > 0) mList.clear();

                            List<BusRouteData> getData = response.body().data;
                            if (getData != null){
                                //운행중?
                                if(_currentData.isDrive.equals("Y")) {
                                    Optional optional = getData.stream().filter(t->"Y".equals(t.isArrive)).reduce((first, second) -> second);
                                    if(optional.isPresent()) {
                                        BusRouteData lastArrived = (BusRouteData) optional.get();
                                        lastArrived.isAtThisStop = true;
                                        int position = getData.indexOf(lastArrived);
                                        if(_positionBus != position) {
                                            _positionBus = position;
                                            isScrollY = true;
                                        }
                                    }
                                }else{

                                }
                                if (mList != null) mList.addAll(getData);

                            }
                        }

                    } else {

                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_route_binding_error, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.bus_route_not_found, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mRecyclerRoute.smoothScrollToPosition(_positionBus);
                    if (mList != null) mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    setProgressWithAnimation();
                    _handler.sendEmptyMessageDelayed(CMD_REFRESH, REFRESH_SEC * 1000);
                }

                @Override
                public void onFailure(Call<BusRouteResponse> call, Throwable t) {
                    LogMgr.e(TAG, "request() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    setProgressWithAnimation();
                    _handler.sendEmptyMessageDelayed(CMD_REFRESH, REFRESH_SEC * 1000);

                }
            });
        }

    }
    public void doFABAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(fabBtn != null) {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                    fabBtn.startAnimation(animation);
                }
            }
        });

    }
    public void setProgressWithAnimation() {
        animation = ObjectAnimator.ofInt (_fabProgres, "progress", 0, 100); // see this max value coming back here, we animale towards that value
        animation.setDuration (REFRESH_SEC * 1000); //in milliseconds
        animation.setInterpolator (new LinearInterpolator());
        animation.start ();
    }
}