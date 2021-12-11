package com.xzy.study.messengerclient;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 代码也不复杂，首先bindService，然后在onServiceConnected中拿到回调的service（IBinder）对象，
 * 通过service对象去构造一个mService =new Messenger(service);然后就可以使用mService.send(msg)给服务端了。
 * 原文链接：https://blog.csdn.net/lmj623565791/article/details/47017485
 *
 * @author xzy
 */
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MSG_SUM = 0x110;

    private Button mBtnAdd;
    private LinearLayout mLyContainer;
    /**
     * 显示连接状态
     */
    private TextView mTvState;

    private Messenger mService;
    private boolean isConn;

    /**
     * 构建 Messenger 实例和回调
     */
    @SuppressLint("handlerLeak")
    private final Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what) {
                case MSG_SUM:
                    TextView tv = (TextView) mLyContainer.findViewById(msgFromServer.arg1);
                    tv.setText(tv.getText() + "=>" + msgFromServer.arg2);
                    break;
                default:
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });

    /**
     * 构建 ServiceConnection 实例
     */
    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            isConn = true;
            mTvState.setText("connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isConn = false;
            mTvState.setText("disconnected!");
        }
    };

    private int mA;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 开始绑定服务
        bindServiceInvoked();

        mTvState = (TextView) findViewById(R.id.id_tv_callback);
        mBtnAdd = (Button) findViewById(R.id.id_btn_add);
        mLyContainer = (LinearLayout) findViewById(R.id.id_ll_container);

        mBtnAdd.setOnClickListener(v -> {
            try {
                int a = mA++;
                int b = (int) (Math.random() * 100);

                // 创建一个 tv,添加到 LinearLayout中
                TextView tv = new TextView(MainActivity.this);
                tv.setText(a + " + " + b + " = calculating ...");
                tv.setId(a);
                mLyContainer.addView(tv);

                Message msgFromClient = Message.obtain(null, MSG_SUM, a, b);
                msgFromClient.replyTo = mMessenger;
                if (isConn) {
                    //往服务端发送消息
                    mService.send(msgFromClient);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

    }

    private void bindServiceInvoked() {
        Intent intent = new Intent();
        intent.setAction("com.xzy.aidl.messenger");
        intent.setPackage("com.xzy.study.messengerserver");
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "bindService invoked !");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }
}
