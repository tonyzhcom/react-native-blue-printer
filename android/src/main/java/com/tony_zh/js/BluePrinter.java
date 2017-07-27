package com.tony_zh.js;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 * Created by tony on 2017/6/13.
 */

public class BluePrinter extends ReactContextBaseJavaModule {
    private static final int D58MMWIDTH = 384;
    private static final int D80MMWIDTH = 576;

    public static BluetoothService mService = null;


    public BluePrinter(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "BluePrinter";
    }

    @ReactMethod
    public void connect(final Callback cb) {

        mService = new BluetoothService(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BluetoothService.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:   //已连接
                                Log.d("蓝牙调试","success.....");
                                cb.invoke("connected");
                                break;
                            case BluetoothService.STATE_CONNECTING:  //正在连接
                                Log.d("蓝牙调试","正在连接.....");
                                break;
                            case BluetoothService.STATE_LISTEN:     //监听连接的到来
                            case BluetoothService.STATE_NONE:
                                Log.d("蓝牙调试","等待连接.....");
                                break;
                        }
                        break;
                    case BluetoothService.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                        Log.d("蓝牙调试","gone");
                        break;
                    case BluetoothService.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                        Log.d("蓝牙调试","fail .....");
                        break;
                }
            }

        });
        Set<BluetoothDevice> pairedDevices = mService.getPairedDev();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("found ~",device.getName() + ":" + device.getAddress());
            }
        } else {
            Log.d("found ~","none");
        }
        BluetoothDevice con_dev = mService.getDevByMac("98:5D:AD:10:AD:EB");
        mService.connect(con_dev);
    }
    @ReactMethod
    public void check(Callback cb) {
        boolean s = false ;
        if(mService != null){
            s = mService.isAvailable();
        }
        Log.d("Blueprinter","s:"+String.valueOf(s));
        cb.invoke(s);

    }
    @ReactMethod
    public void print(String imgurl,Callback cb) {
        if(mService == null || !mService.isAvailable()){
            cb.invoke("print wrong disconnect");
            return ;
        }
        byte[] sendData = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imgurl);
            Bitmap bm  = BitmapFactory.decodeStream(fis);
            int height = D58MMWIDTH * bm.getHeight() / bm.getWidth();
            bm = Bitmap.createScaledBitmap(bm, D58MMWIDTH, height, false);
            sendData = PrintPicture.POS_PrintBMP(bm, D58MMWIDTH, 0);
            mService.write(sendData);   //打印byte流数据
            Log.d("打印了","打印了 ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @ReactMethod
    public void print_p(String imgurl,Promise promise) {
        ensureConnect();

        byte[] sendData = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imgurl);
            Bitmap bm  = BitmapFactory.decodeStream(fis);
            int height = D58MMWIDTH * bm.getHeight() / bm.getWidth();
            bm = Bitmap.createScaledBitmap(bm, D58MMWIDTH, height, false);
            sendData = PrintPicture.POS_PrintBMP(bm, D58MMWIDTH, 0);
            mService.write(sendData);   //打印byte流数据
            Log.d("打印了","打印了 ");
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("blue printer check error!", e);
        }

    }


    @ReactMethod
    public void check_p(Promise promise) {
        ensureConnect();
        try {
            boolean s = false ;
            if(mService != null){
                s = mService.isAvailable();
            }
            promise.resolve(s);
        } catch (Exception e) {
            promise.reject("blue printer check error!", e);
        }
    }
    @ReactMethod
    public void list_p(Promise promise) {
        ensureConnect();
        try {
            WritableArray ret =  Arguments.createArray();
            Set<BluetoothDevice> pairedDevices = mService.getPairedDev();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    WritableMap map = Arguments.createMap();
                    map.putString("name",device.getName());
                    map.putString("mac",device.getAddress());
                    ret.pushMap(map);
                    Log.d("found ~",device.getName() + ":" + device.getAddress());
                }
            } else {
                Log.d("found ~","none");
            }
            promise.resolve(ret);
        } catch (Exception e) {
            promise.reject("blue printer check error!", e);
        }
    }
    @ReactMethod
    public void connect_p(String mac,Promise promise) {
        ensureConnect();
        try {
            BluetoothDevice con_dev = mService.getDevByMac(mac);
            mService.connect(con_dev);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("blue printer check error!", e);
        }
    }

    public void ensureConnect() {
        if(mService == null){
          mService = new BluetoothService(new Handler() {
              @Override
              public void handleMessage(Message msg) {
                  switch (msg.what) {
                      case BluetoothService.MESSAGE_STATE_CHANGE:
                          switch (msg.arg1) {
                              case BluetoothService.STATE_CONNECTED:   //已连接
                                  Log.d("蓝牙调试","success.....");
                                  break;
                              case BluetoothService.STATE_CONNECTING:  //正在连接
                                  Log.d("蓝牙调试","正在连接.....");
                                  break;
                              case BluetoothService.STATE_LISTEN:     //监听连接的到来
                              case BluetoothService.STATE_NONE:
                                  Log.d("蓝牙调试","等待连接.....");
                                  break;
                          }
                          break;
                      case BluetoothService.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                          Log.d("蓝牙调试","gone");
                          break;
                      case BluetoothService.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                          Log.d("蓝牙调试","fail .....");
                          break;
                  }
              }

          });
        }

    }



}
