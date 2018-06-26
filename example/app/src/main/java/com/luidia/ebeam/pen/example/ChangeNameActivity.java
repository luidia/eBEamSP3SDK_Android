package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luidia.ebeam.pen.sdk.constants.PenMessage;
import com.luidia.ebeam.pen.sdk.listener.PenMessageListener;

public class ChangeNameActivity extends BaseActivity implements View.OnClickListener, PenMessageListener {
    private EditText etNewName;
    private TextView tvNamePrefix;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        tvNamePrefix = findViewById(R.id.tv_name_prefix);
        etNewName = findViewById(R.id.et_new_name);
        findViewById(R.id.btn_change).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        penController.setPenMessageListener(this);
        penController.setPenEventListener(null);
        updateWidgets();
    }

    @Override
    protected void onPause() {
        penController.setPenMessageListener(null);
        penController.setPenEventListener(null);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_change:
                penController.setDeviceName(etNewName.getText().toString());
                break;
        }
    }

    @Override
    public void onPenMessage(int i, int i1, int i2, Object o) {
        final int what = i;

        switch(what) {
            case PenMessage.PNF_MSG_DI_OK:
                updateWidgets();
                Toast.makeText(getApplicationContext(), "Changed", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    private void updateWidgets() {
        tvNamePrefix.setText(penController.getDeviceName().substring(0, 4));
        etNewName.setText(penController.getDeviceName().substring(4).trim());
    }
}
