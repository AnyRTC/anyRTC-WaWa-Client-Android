package org.ar.wawa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.anyrtc.common.utils.ScreenUtils;
import org.ar.tools.PermissionsCheckUtil;
import org.ar.tools.SCommonItemDecoration;
import org.ar.wawaClient.ARWaWaClient;
import org.ar.wawaClient.WaWaServerListener;
import org.ar.weight.CustomDialog;

import java.util.List;

import butterknife.BindView;

public class WaWaListActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener, WaWaServerListener, SwipeRefreshLayout.OnRefreshListener {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    Gson gson;
    RoomAdapter roomAdapter;
    View view;
    CustomDialog customDialog;
    @BindView(R.id.card)
    CardView card;
    public static boolean isFirst=true;
    @Override
    public int getLayoutId() {
        return R.layout.activity_wa_wa_list;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        view = findViewById(R.id.space);
        mImmersionBar.titleBar(view).statusBarDarkFont(true, 0.2f).init();
        recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setNestedScrollingEnabled(false);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        ARWaWaClient.getInstance().setServerListener(this);
        if (!ARWaWaClient.hadConnSocketServer) {
            ARWaWaClient.getInstance().openServer();
        }
        if (ARWaWaClient.hadConnSocketServer && ARWaWaClient.hadConnAnyRTCServer) {
            ARWaWaClient.getInstance().getRoomList();
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2,GridLayoutManager.VERTICAL, false));
        gson = new Gson();
        int h = ScreenUtils.dip2px(this, 18);
        int v = ScreenUtils.dip2px(this, 18);
        SparseArray<SCommonItemDecoration.ItemDecorationProps> propMap = new SparseArray<>();
        SCommonItemDecoration.ItemDecorationProps prop1 =
                new SCommonItemDecoration.ItemDecorationProps(v, h, true, true);
        propMap.put(RoomAdapter.TYPE_1, prop1);
        recyclerView.addItemDecoration(new SCommonItemDecoration(propMap));
        roomAdapter = new RoomAdapter();
        roomAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(roomAdapter);
        findViewById(R.id.call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCall();
            }
        });
    }

    public void showCall() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        customDialog = builder.setContentView(R.layout.dialog_base_layout_two_btn)
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setAnimId(R.style.default_dialog_style)
                .setBackgroundDrawable(true)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(CustomDialog customDialog) {
                        TextView content = (TextView) customDialog.findViewById(R.id.content);
                        content.setText("联系商务:021-65650071-840 ?");
                        TextView tv_no = (TextView) customDialog.findViewById(R.id.tv_no);
                        TextView tv_ok = (TextView) customDialog.findViewById(R.id.tv_ok);
                        tv_ok.setText("商务咨询");
                        tv_no.setOnClickListener(new DialogOnclick());
                        tv_ok.setOnClickListener(new DialogOnclick());
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            showCameraAnim();
        }
    }

    public class DialogOnclick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_no:
                    if (customDialog != null) {
                        customDialog.dismiss();
                    }
                    break;
                case R.id.tv_ok:
                    Uri uri = Uri.parse("tel:021-65650071-840");
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, final int position) {
        if (roomAdapter.getItem(position).getRoom_state() != 0) {
            Toast.makeText(WaWaListActivity.this, "娃娃机维护中...", Toast.LENGTH_SHORT).show();
            return;
        }

            if (AndPermission.hasPermissions(WaWaListActivity.this, Permission.CAMERA,Permission.RECORD_AUDIO)){
            Intent intent = new Intent(WaWaListActivity.this, WaWaActivity.class);
            intent.putExtra("room", roomAdapter.getItem(position));
            startActivity(intent);
        }else {
            AndPermission.with(WaWaListActivity.this).runtime().permission(Permission.RECORD_AUDIO,Permission.CAMERA).onGranted(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    Intent intent = new Intent(WaWaListActivity.this, WaWaActivity.class);
                    intent.putExtra("room", roomAdapter.getItem(position));
                    startActivity(intent);
                }
            }).onDenied(new Action<List<String>>() {
                @Override
                public void onAction(List<String> data) {
                    PermissionsCheckUtil.showMissingPermissionDialog(WaWaListActivity.this, "请先开启录音和相机权限");
                }
            }).start();
        }
    }


    @Override
    public void onConnectServerSuccess() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onReconnect() {

    }

    @Override
    public void onInitAnyRTCSuccess() {
        WaWaListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ARWaWaClient.getInstance().getRoomList();
            }
        });
    }

    @Override
    public void onInitAnyRTCFaild() {

    }


    @Override
    public void onGetRoomList(final String data) {
        WaWaListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                RoomBean roomBean = gson.fromJson(data, RoomBean.class);
                roomAdapter.setNewData(roomBean.getData().getRoomlist());
            }
        });
    }

    @Override
    public void onJoinRoom(int code, String videoInfo, String strMemberNum) {

    }

    @Override
    public void onLeaveRoom(int code) {

    }

    @Override
    public void onWaWaLeave(int code) {

    }

    @Override
    public void onBookResult(int code, String strBookNum) {

    }

    @Override
    public void onUnBookResult(int code, String strBookNum) {

    }

    @Override
    public void onControlCmd(int code, String cmd) {

    }

    @Override
    public void onRoomUrlUpdate(String strVideoInfo) {

    }


    @Override
    public void onBookMemberUpdate(String num) {

    }

    @Override
    public void onRoomMemberUpdate(String strMemberNum) {

    }


    @Override
    public void onReadyStart() {

    }

    @Override
    public void onReadyTimeout() {

    }

    @Override
    public void onPlayTimeout() {

    }


    @Override
    public void onResult(boolean result) {

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ARWaWaClient.getInstance().setServerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (!ARWaWaClient.hadConnSocketServer) {
            Toast.makeText(this, "未连接到服务器", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            ARWaWaClient.getInstance().openServer();
            return;
        }
        if (!ARWaWaClient.hadConnAnyRTCServer) {
            Toast.makeText(this, "未初始化anyRTC信息", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (ARWaWaClient.hadConnSocketServer && ARWaWaClient.hadConnAnyRTCServer) {
            ARWaWaClient.getInstance().getRoomList();
        }
    }

    public void showCameraAnim() {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -2f, Animation.RELATIVE_TO_SELF, 0f);
        translateAnimation.setInterpolator(new AnticipateOvershootInterpolator());
        translateAnimation.setDuration(1000);
        card.startAnimation(translateAnimation);
        isFirst=false;
    }
}
