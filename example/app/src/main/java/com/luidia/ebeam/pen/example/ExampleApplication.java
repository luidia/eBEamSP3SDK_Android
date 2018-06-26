package com.luidia.ebeam.pen.example;

/*
    eBeam Smartpen SDK
    Copyright (c) 2018 Luidia Global, Inc.
    https://www.luidia.com/
*/

import com.luidia.ebeam.pen.sdk.EBeamSPController;

public class ExampleApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        EBeamSPController.create(this);
    }
}
