package com.example.testblutooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 0x11;

    Button btn_open, btn_close, btn_found, btn_connected;
    TextView headView;
    ListView blutetoothList;
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    HashMap<String, BluetoothDevice> deviceMap;
    ArrayAdapter<String> mArrayAdapter;


    static WindowManager windowManager;
    static WindowManager.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headView = new TextView(this);
        btn_open = (Button) findViewById(R.id.btn_open);
        btn_connected = (Button) findViewById(R.id.btn_connected);
        btn_close = (Button) findViewById(R.id.btn_close);
        btn_found = (Button) findViewById(R.id.btn_found);
        blutetoothList = (ListView) findViewById(R.id.list);

        btn_open.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        btn_found.setOnClickListener(this);
        btn_connected.setOnClickListener(this);

        deviceMap = new HashMap<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        blutetoothList.addHeaderView(headView);

        blutetoothList.setOnItemClickListener(this);

//        showConnedtedBluetoothList();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            }else {
                initWindowManagerView(MainActivity.this);
            }
        }

    }

    private void initWindowManagerView(MainActivity activity) {

        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        //设置效果为背景透明.
//        params.format = PixelFormat.RGBA_8888;
        params.format = Color.RED;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 200;
        params.y = 100;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = 300;
        params.height = 300;

        windowManager.addView(blutetoothList, params);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null) {
                    int deviceType = device.getBluetoothClass().getDeviceClass();
                    if (deviceType == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
                            || deviceType == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
                        mArrayAdapter.add(device.getName() + "-audio_video- ==" + device.getAddress());
//                        Log.i(TAG, " devices getDeviceClass-> " + device.getBluetoothClass().getDeviceClass());
                    } else {
                        mArrayAdapter.add(device.getName() + "==" + device.getAddress());
                    }
                    Log.i(TAG, "ACTION_FOUND:  name -> " + device.getName() + ", address -> " + device.getAddress());
                    deviceMap.put(device.getAddress(), device);
                    mArrayAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(TAG, "onReceive:  搜索完毕!!");
            }
        }
    };

    public void showConnedtedBluetoothList() {
        mArrayAdapter.clear();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        headView.setText("设备列表：");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            headView.setText("");
        }


        blutetoothList.setAdapter(mArrayAdapter);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick() called with: v = [" + v + "]");
        switch (v.getId()) {
            case R.id.btn_open:
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent requestIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(requestIntent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(this, "蓝牙已经是打开状态！！", Toast.LENGTH_SHORT).show();
                }
                showConnedtedBluetoothList();
                break;
            case R.id.btn_connected:
                showConnedtedBluetoothList();
                break;
            case R.id.btn_close:
                mBluetoothAdapter.disable();
                showConnedtedBluetoothList();
                break;
            case R.id.btn_found:
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mArrayAdapter.clear();
                mArrayAdapter.notifyDataSetChanged();
                mBluetoothAdapter.startDiscovery();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Log.i(TAG, "onActivityResult: resultCode -> " + resultCode);
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "打开蓝牙成功!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "打开蓝牙失败!!", Toast.LENGTH_SHORT).show();
                }
                break;
            case 100:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        initWindowManagerView(MainActivity.this);
                    } else {
                        Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION权限已被拒绝", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String bluetooth = (String) blutetoothList.getItemAtPosition(position);
        String[] tmparray = bluetooth.split("==");

        Log.i(TAG, "onItemClick:  map size -> " + deviceMap.size());
        BluetoothDevice device = deviceMap.get(tmparray[1]);
        Log.i(TAG, "onItemClick: BluetoothDevice -> " + device.getName() + "," + device.getAddress());
        connect(device);
    }

    private void connect(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        try {
            Method method = BluetoothDevice.class.getMethod("createBond");
            method.invoke(device);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "connect exception:" + e);
            e.printStackTrace();
        }
    }
}
