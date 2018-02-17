package com.goldminer.secugendemo;

import android.content.Context;
import android.hardware.usb.UsbManager;

import SecuGen.FDxSDKPro.JSGFPLib;

public class SecuGenManager {

    private static SecuGenManager instance = null;

    private JSGFPLib sgfplib = null;

    public static SecuGenManager getInstance(Context context) {
        if(SecuGenManager.instance == null) {
            SecuGenManager.instance = new SecuGenManager(context);
        }

        return instance;
    }

    public SecuGenManager(Context context) {
        sgfplib = new JSGFPLib((UsbManager) context.getSystemService(Context.USB_SERVICE));
    }

    public JSGFPLib getJSGFPLib() {
        return this.sgfplib;
    }
}
