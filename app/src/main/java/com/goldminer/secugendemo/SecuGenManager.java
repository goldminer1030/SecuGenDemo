package com.goldminer.secugendemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGAutoOnEventNotifier;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;

public class SecuGenManager {

    private static SecuGenManager instance = null;
    private SGAutoOnEventNotifier autoOn = null;
    private PendingIntent mPermissionIntent = null;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private JSGFPLib sgfplib = null;

    public static SecuGenManager getInstance(Context context) {
        if(SecuGenManager.instance == null) {
            SecuGenManager.instance = new SecuGenManager(context);
        }

        return instance;
    }

    public SecuGenManager(Context context) {
        sgfplib = new JSGFPLib((UsbManager) context.getSystemService(Context.USB_SERVICE));
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    public long init() {
        return sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
    }

    public void close() {
        sgfplib.CloseDevice();
        sgfplib.Close();
    }

    public long openDevice() {
        UsbDevice usbDevice = sgfplib.GetUsbDevice();
        sgfplib.GetUsbManager().requestPermission(usbDevice, mPermissionIntent);
        return sgfplib.OpenDevice(0);
    }

    public long getDeviceInfo(SGDeviceInfoParam param) {
        return sgfplib.GetDeviceInfo(param);
    }

    public long createTemplate(SGFingerInfo fpInfo, byte[] bufImage, byte[] bufTemplate) {
        return sgfplib.CreateTemplate(fpInfo, bufImage, bufTemplate);
    }

    public long setTemplateFormat(short format) {
        return sgfplib.SetTemplateFormat(format);
    }

    public long getVersion() {
        return sgfplib.Version();
    }

    public void getMaxTemplateSize(int[] size) {
        sgfplib.GetMaxTemplateSize(size);
    }

    public void setSmartCapture(boolean enabled) {
        if(enabled) {
            sgfplib.WriteData((byte) 5, (byte) 1);
        } else {
            sgfplib.WriteData((byte) 5, (byte) 0);
        }
    }

    public void setSmartCaptureModeN(boolean enabled) {
        if(enabled) {
            sgfplib.WriteData((byte) 0, (byte) 0);
        } else {
            sgfplib.WriteData((byte) 0, (byte) 1);
        }
    }

    public void stop() {
        stopAutoOn();
        sgfplib.CloseDevice();
    }

    public UsbDevice getUsbDevice() {
        return sgfplib.GetUsbDevice();
    }

    public void startAutoOn() {
        if(autoOn != null) {
            autoOn.start();
        }
    }

    public void stopAutoOn() {
        if(autoOn != null) {
            autoOn.stop();
        }
    }

    public long matchTemplate(byte[] bufRegisterTemplate, byte[] bufVerifyTemplate, long securityLevel, boolean[] bMatched) {
        return sgfplib.MatchTemplate(bufRegisterTemplate, bufVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, bMatched);
    }

    public long setLedOn(boolean bLedOn) {
        return sgfplib.SetLedOn(bLedOn);
    }

    public long getImage(byte[] bufImage) {
        return sgfplib.GetImage(bufImage);
    }

    public void dumpFile(String fileName, byte[] buffer) {
//        try {
//            File myFile = new File("/sdcard/Download/" + fileName);
//            myFile.createNewFile();
//            FileOutputStream fOut = new FileOutputStream(myFile);
//            fOut.write(buffer,0,buffer.length);
//            fOut.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public long getImageEx(byte[] buffer, long timeout, long quality) {
        return sgfplib.GetImageEx(buffer, 10000,50);
    }

    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(byte[] mImageBuffer, int nWidth, int nHeight) {
        byte[] Bits = new byte[mImageBuffer.length * 4];
        for (int i = 0; i < mImageBuffer.length; i++) {
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = mImageBuffer[i]; // Invert the source bits
            Bits[i * 4 + 3] = -1;// 0xff, that's the alpha.
        }

        Bitmap bmpGrayscale = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
        //Bitmap bm contains the fingerprint img
        bmpGrayscale.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        return bmpGrayscale;
    }


    //Converts image to grayscale (NEW)
    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y=0; y< height; ++y) {
            for (int x=0; x< width; ++x){
                int color = bmpOriginal.getPixel(x, y);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                int gray = (r+g+b)/3;
                color = Color.rgb(gray, gray, gray);
                //color = Color.rgb(r/3, g/3, b/3);
                bmpGrayscale.setPixel(x, y, color);
            }
        }
        return bmpGrayscale;
    }

    //Converts image to binary (OLD)
    public Bitmap toBinary(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public long readSerialNumber(byte[] serialNumber) {
        return sgfplib.ReadSerialNumber(serialNumber);
    }

    public void createSGAutoOnEventNotifier(SGFingerPresentEvent event) {
        autoOn = new SGAutoOnEventNotifier (sgfplib, event);
    }

    public JSGFPLib getJSGFPLib() {
        return this.sgfplib;
    }
}
