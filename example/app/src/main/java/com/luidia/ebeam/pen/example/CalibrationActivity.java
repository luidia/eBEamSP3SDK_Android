package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.luidia.ebeam.pen.sdk.constants.PenEvent;
import com.luidia.ebeam.pen.sdk.listener.CalibrationResultCallback;
import com.luidia.ebeam.pen.sdk.listener.PenEventListener;

public class CalibrationActivity extends BaseActivity implements PenEventListener {
    private final static int TOP_LEFT = 0;
    private final static int BOTTOM_LEFT = 3;
    private final static int BOTTOM_RIGHT = 2;

    private ImageView ivTopLeft;
    private ImageView ivBottomLeft;
    private ImageView ivBottomRight;

    private ImageView currentPosition;

    private PointF[] calibrationPositionData = new PointF[]{new PointF(), new PointF(), new PointF(), new PointF()};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        ivTopLeft = findViewById(R.id.iv_top_left);
        ivBottomLeft = findViewById(R.id.iv_bottom_left);
        ivBottomRight = findViewById(R.id.iv_bottom_right);

        currentPosition = ivTopLeft;
        currentPosition.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        penController.setPenMessageListener(null);
        penController.setPenEventListener(this);
    }

    @Override
    protected void onPause() {
        penController.setPenMessageListener(null);
        penController.setPenEventListener(null);
        super.onPause();
    }

    @Override
    public void onPenEvent(int i, int i1, int i2, Object o) {
        final int what = i;
        final int x = i1;
        final int y = i2;

        switch (what) {
            case PenEvent.PEN_UP:
                if (currentPosition == ivTopLeft) {
                    calibrationPositionData[TOP_LEFT] = new PointF(x, y);

                    currentPosition.setVisibility(View.GONE);
                    currentPosition = ivBottomLeft;
                    currentPosition.setVisibility(View.VISIBLE);
                } else if (currentPosition == ivBottomLeft) {
                    calibrationPositionData[BOTTOM_LEFT] = new PointF(x, y);

                    currentPosition.setVisibility(View.GONE);
                    currentPosition = ivBottomRight;
                    currentPosition.setVisibility(View.VISIBLE);
                } else if (currentPosition == ivBottomRight) {
                    calibrationPositionData[BOTTOM_RIGHT] = new PointF(x, y);

                    currentPosition.setVisibility(View.GONE);
                    currentPosition = null;

                    penController.requestCalibrationChanging(calibrationPositionData[TOP_LEFT],
                            calibrationPositionData[BOTTOM_RIGHT],
                            new CalibrationResultCallback() {
                                @Override
                                public void onCalibrationCompleted() {
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Calibration changed!", Toast.LENGTH_SHORT).show());
                                    finish();
                                }

                                @Override
                                public void onCalibrationFailed() {
                                    Toast.makeText(getApplicationContext(), "Calibration failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
        }
    }
}
