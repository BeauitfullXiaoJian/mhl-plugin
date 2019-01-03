package com.plugin;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.qs.helper.printer.BlueToothService;
import com.qs.helper.printer.PrinterClass;
import com.qs.helper.printer.bt.BtService;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class MHLPlugin extends CordovaPlugin implements
        BlueToothService.OnReceiveDataHandleEvent {

    private static final String TAG = "MHLPluginLog";

    private CallbackContext mCallbackContext;

    private BlueToothService mBlueToothService;
    private BtService mBTService;
    private ArrayList<DeviceData> mBluetoothDevices = new ArrayList<DeviceData>();

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        mBTService = new BtService(cordova.getActivity(),
                BluetoothServiceHandler.getInstance(MHLPlugin.this), null);
        mBlueToothService = BtService.mBTService;
        if (!mBlueToothService.IsOpen()) {
            mBlueToothService.OpenDevice();
        }
        this.mBlueToothService.setOnReceive(this);
    }

    @Override
    public void OnReceive(BluetoothDevice device) {
        if (device != null) {
            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceName(device.getName());
            deviceData.setDeviceAddress(device.getAddress());
            mBluetoothDevices.add(deviceData);
            Log.d(TAG, "设备数量:" + mBluetoothDevices.size());
            Log.d(TAG, String.format("设备名称:%s,设备地址:%s", device.getName(), device.getAddress()));
        } else {
            Log.d(TAG, "扫描结束");
            JSONArray array = new JSONArray();
            try {
                for (DeviceData data : mBluetoothDevices) {
                    JSONObject object = new JSONObject();
                    object.put("deviceName", data.getDeviceName());
                    object.put("deviceAddress", data.getDeviceAddress());
                    array.put(object);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                mCallbackContext.success(array);
            }
        }
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        Log.d(TAG, "调用方法" + action);
        if (action.equals("devices")) {
            Log.d(TAG, "重新扫描设备");
            mBluetoothDevices.clear();
            mBlueToothService.ScanDevice();
        } else if (action.equals("connect")) {
            Log.d(TAG, "连接到新设备");
            String address = args.getString(0);
            this.mBlueToothService.ConnectToDevice(address);
        }else if (action.equals("print")) {
            Log.d(TAG, "打印数据");
            String message = args.getString(0);
            this.mBTService.printText(message);
            this.mBTService.printText("\n");
            this.mBTService.write(new byte[]{0x1d, 0x0c});
        }
        return true;
    }

    class DeviceData {
        private String deviceName;
        private String deviceAddress;

        String getDeviceName() {
            return deviceName == null ? "未知设备" : deviceName;
        }

        void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        String getDeviceAddress() {
            return deviceAddress;
        }

        void setDeviceAddress(String deviceAddress) {
            this.deviceAddress = deviceAddress;
        }
    }

    static class BluetoothServiceHandler extends Handler {

        private static BluetoothServiceHandler sInstance;

        private static BluetoothServiceHandler getInstance(MHLPlugin plugin) {
            if (sInstance == null) {
                sInstance = new BluetoothServiceHandler(plugin);
            }
            return sInstance;
        }

        WeakReference<MHLPlugin> mPlugin;

        BluetoothServiceHandler(MHLPlugin plugin) {
            mPlugin = new WeakReference<MHLPlugin>(plugin);
        }

        @Override
        public void handleMessage(Message msg) {
            CallbackContext callbackContext = mPlugin.get().mCallbackContext;
            Log.d(TAG, "接收到蓝牙服务消息,状态码" + msg.what);
            if (msg.what == 1) {
                switch (msg.arg1) {
                    case PrinterClass.STATE_CONNECTING: {
                        Log.d(TAG, "正在连接到设备");
                        break;
                    }
                    case PrinterClass.SUCCESS_CONNECT: {
                        Log.d(TAG, "设备连接成功");
                        callbackContext.success("设备连接成功");
                        break;
                    }
                    case PrinterClass.FAILED_CONNECT: {
                        Log.d(TAG, "设备连接失败");
                        callbackContext.error("设备连接失败");
                        break;
                    }
                    case PrinterClass.LOSE_CONNECT: {
                        Log.d(TAG, "设备断开连接");
                        break;
                    }
                }
            }
        }
    }
}