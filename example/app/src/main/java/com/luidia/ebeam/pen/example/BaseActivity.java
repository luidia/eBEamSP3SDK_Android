package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import android.app.Activity;

import com.luidia.ebeam.pen.sdk.EBeamSPController;

public class BaseActivity extends Activity {
    protected EBeamSPController penController = EBeamSPController.getInstance();
}
