package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.luidia.ebeam.pen.sdk.PenDataClass;
import com.luidia.ebeam.pen.sdk.constants.PenEvent;
import com.luidia.ebeam.pen.sdk.constants.PenMessage;
import com.luidia.ebeam.pen.sdk.listener.PenEventListener;
import com.luidia.ebeam.pen.sdk.listener.PenMessageListener;

public class MainActivity extends BaseActivity implements View.OnClickListener, PenEventListener, PenMessageListener {
    private final static int TOP_LEFT = 0;
    private final static int TOP_RIGHT = 1;
    private final static int BOTTOM_LEFT = 3;
    private final static int BOTTOM_RIGHT = 2;

    private TextView tvDevInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_calibration).setOnClickListener(this);
        findViewById(R.id.btn_change_name).setOnClickListener(this);

        tvDevInfo = findViewById(R.id.tv_dev_info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        penController.setPenMessageListener(this);
        penController.setPenEventListener(this);

        if (penController.isPenMode()) {
            penController.setProjectiveLevel(4);

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            PointF[] drawingArea = new PointF[4];
            drawingArea[TOP_LEFT] = new PointF(0, 0);
            drawingArea[TOP_RIGHT] = new PointF(metrics.widthPixels, 0);
            drawingArea[BOTTOM_LEFT] = new PointF(0, metrics.heightPixels);
            drawingArea[BOTTOM_RIGHT] = new PointF(metrics.widthPixels, metrics.heightPixels);

            penController.setCalibrationData(drawingArea, 0, penController.getCalibrationPoint());
        }

        updateButtons();
        updateDeviceInfo();
    }

    @Override
    protected void onPause() {
        penController.setPenMessageListener(null);
        penController.setPenEventListener(null);

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_connect:
                if (penController.isPenMode()) {
                    penController.disconnect();
                } else {
                    i = new Intent(this, ConnectionActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_calibration:
                if (!penController.isPenMode()) {
                    Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                } else {
                    i = new Intent(this, CalibrationActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_change_name:
                if (!penController.isPenMode()) {
                    Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                } else {
                    i = new Intent(this, ChangeNameActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void onPenEvent(int i, int i1, int i2, Object o) {
        final int what = i;
        final int x = i1;
        final int y = i2;

        final PenDataClass penData = (o instanceof PenDataClass) ? (PenDataClass) o : null;
        if (penData != null) {
            PointF pos = penController.getCoordinatePosition(x, y, penData.bRight);

            if (pos == null) {
                pos = new PointF(0, 0);
            }

            updateDeviceInfo(what, pos.x, pos.y);
        }
    }

    @Override
    public void onPenMessage(int i, int i1, int i2, Object o) {
        final int what = i;

        switch (what) {
            case PenMessage.PNF_MSG_DISCONNECTED:
            case PenMessage.PNF_MSG_CONNECTED:
            case PenMessage.PNF_MSG_DI_FAIL:
                updateButtons();
                updateDeviceInfo();
                break;
        }
    }

    private void updateButtons() {
        Button btn = findViewById(R.id.btn_connect);
        if (penController.isPenMode()) {
            btn.setText("disconnect");
        } else {
            btn.setText("connect");
        }
    }

    private void updateDeviceInfo() {
        updateDeviceInfo(PenEvent.PEN_UP, 0, 0);
    }

    private void updateDeviceInfo(int penEvent, float x, float y) {
        if (penController.isPenMode()) {
            final String connectionState = "connected";
            final String deviceName = penController.getDeviceName();
            final String deviceAddress = penController.getDeviceAddress();

            String sb = "Connection state : [" + connectionState + "]\n" +
                    "Device Name : [" + deviceName + "]\n" +
                    "Device Address : [" + deviceAddress + "]\n" +
                    "\n\n\n" +
                    "\t=== Pen Event ===" + "\n" +
                    "pen status = [" + getPenEvent(penEvent) + "]\n" +
                    "x = [" + x + "]\t\t" +
                    "y = [" + y + "]\n";

            tvDevInfo.setText(sb);
        } else {
            tvDevInfo.setText("Disconnected");
        }
    }

    private String getPenEvent(int penEvent) {
        if (penEvent == PenEvent.PEN_DOWN) {
            return "DOWN";
        }

        if (penEvent == PenEvent.PEN_MOVE) {
            return "MOVE";
        }

        if (penEvent == PenEvent.PEN_HOVER) {
            return "HOVER";
        }

        return "UP";
    }
}
