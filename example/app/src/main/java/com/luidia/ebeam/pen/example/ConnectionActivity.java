package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.luidia.ebeam.pen.sdk.constants.PenMessage;
import com.luidia.ebeam.pen.sdk.listener.PenMessageListener;

import java.util.ArrayList;
import java.util.List;

public class ConnectionActivity extends BaseActivity implements PenMessageListener, AdapterView.OnItemClickListener {
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_PERMISSION = 2;

    private ListView listDevices;
    private DeviceListAdapter deviceListAdapter;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        listDevices = findViewById(R.id.list_devices);
        listDevices.setDivider(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorPrimary)}));
        listDevices.setDividerHeight(1);
        listDevices.setOnItemClickListener(this);

        deviceListAdapter = new DeviceListAdapter(this);
        listDevices.setAdapter(deviceListAdapter);

        if (!penController.isBluetoothEnabled()) {
            goBluetoothEnable();
        } else {
            startSearchingForBluetoothDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == REQUEST_ENABLE_BT) {
            startSearchingForBluetoothDevices();
        } else if(resultCode == REQUEST_PERMISSION) {
            if(checkPermissions()) {
                startSearchingForBluetoothDevices();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!checkPermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION);
        }

        penController.setPenEventListener(null);
        penController.setPenMessageListener(this);
    }

    @Override
    protected void onPause() {
        penController.setPenEventListener(null);
        penController.setPenMessageListener(null);

        super.onPause();
    }

    @Override
    public void onPenMessage(int i, int i1, int i2, Object o) {
        if(i == PenMessage.PNF_MSG_FIRST_DATA_RECV) {
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final DeviceInfo info = (DeviceInfo) deviceListAdapter.getItem(position);
        final String address = info.device.getAddress();
        final String name = info.device.getName();

        penController.connect(name, address);
        Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_LONG).show();
    }

    private void goBluetoothEnable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }

    private List<String> addresses;
    private void startSearchingForBluetoothDevices() {
        addresses = new ArrayList<>();

        penController.getBluetoothAdapter().stopLeScan(bluetoothScanCallback);

        penController.getBluetoothAdapter().cancelDiscovery();
        penController.getBluetoothAdapter().startLeScan(bluetoothScanCallback);

        handler.removeCallbacksAndMessages(this);
        handler.postDelayed(() -> penController.getBluetoothAdapter().stopLeScan(bluetoothScanCallback), 10000);
    }

    private BluetoothAdapter.LeScanCallback bluetoothScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                if (!TextUtils.isEmpty(device.getName())) {
                    if (device.getName().contains("eSP") || device.getName().contains("eBP")) {
                        if(addresses.size() > 0) {
                            for(String addr : addresses) {
                                if(addr.equals(device.getAddress())) {
                                    return;
                                }
                            }
                        }
                        addresses.add(device.getAddress());
                        deviceListAdapter.put(device, rssi);
                        deviceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

    }

    private class DeviceInfo {
        private BluetoothDevice device;
        private int rssi;

        DeviceInfo(BluetoothDevice device, int rssi) {
            this.device = device;
            this.rssi = rssi;
        }
    }

    private class DeviceListAdapter extends BaseAdapter {
        private List<DeviceInfo> deviceInfoList;
        private LayoutInflater inflater;

        DeviceListAdapter(@NonNull Context context) {
            this.inflater = LayoutInflater.from(context);
            this.deviceInfoList = new ArrayList<>();
        }

        public void put(@NonNull BluetoothDevice deviceInfo, int rssi) {
            deviceInfoList.add(new DeviceInfo(deviceInfo, rssi));
        }

        @Override
        public int getCount() {
            return this.deviceInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;

            if (convertView == null) {
                v = inflater.inflate(R.layout.item_device_list, parent, false);
            } else {
                v = convertView;
            }

            DeviceInfo devInfo = deviceInfoList.get(position);
            final TextView devName = v.findViewById(R.id.tv_dev_name);
            final TextView devAddr = v.findViewById(R.id.tv_dev_addr);

            devName.setText(devInfo.device.getName());
            devAddr.setText(devInfo.device.getAddress());

            return v;
        }
    }
}
