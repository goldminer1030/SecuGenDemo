package com.goldminer.secugendemo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGDeviceInfoParam;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGFingerPresentEvent;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener, SGFingerPresentEvent {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = "SecuGen USB";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button mCapture;
    private Button mButtonRegister;
    private Button mButtonMatch;
    private Button mButtonLed;
    private android.widget.TextView mTextViewResult;
    private android.widget.CheckBox mCheckBoxMatched;
    private Switch mToggleButtonSmartCapture;
    private Switch mToggleButtonCaptureModeN;
    private Switch mToggleButtonAutoOn;
    private ImageView mImageViewFingerprint;
    private ImageView mImageViewRegister;
    private ImageView mImageViewVerify;

    private byte[] mRegisterImage;
    private byte[] mVerifyImage;
    private byte[] mRegisterTemplate;
    private byte[] mVerifyTemplate;

    private int mImageWidth;
    private int mImageHeight;
    private int[] grayBuffer;
    private int[] mMaxTemplateSize;

    private boolean mLed;
    private boolean mAutoOnEnabled;
    private int nCaptureModeN;

    private Bitmap grayBitmap;
    private IntentFilter filter;

    private OnFragmentInteractionListener mListener;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //DEBUG Log.d(TAG,"Enter mUsbReceiver.onReceive()");
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            debugMessage("Vendor ID : " + device.getVendorId() + "\n");
                            debugMessage("Product ID: " + device.getProductId() + "\n");
                        } else {
                            Log.e(TAG, "mUsbReceiver.onReceive() Device is null");
                        }
                    } else {
                        Log.e(TAG, "mUsbReceiver.onReceive() permission denied for device " + device);
                    }
                }
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void debugMessage(String text) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File logFile = new File(externalStorageDir , "SecuGenDemoLog.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());
            buf.append(strDate);
            buf.append(" ");
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void CaptureFingerPrint(){
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
        this.mCheckBoxMatched.setChecked(false);
        byte[] buffer = new byte[mImageWidth*mImageHeight];
        dwTimeStart = System.currentTimeMillis();

        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
        long result = sgManager.getImageEx(buffer, 10000,50);
        sgManager.dumpFile("capture.raw", buffer);
        dwTimeEnd = System.currentTimeMillis();
        dwTimeElapsed = dwTimeEnd-dwTimeStart;
        debugMessage("getImageEx(10000,50) ret:" + result + " [" + dwTimeElapsed + "ms]\n");
        mTextViewResult.setText("getImageEx(10000,50) ret: " + result + " [" + dwTimeElapsed + "ms]\n");

        //Read Serial number
        byte[] szSerialNumber = new byte[15];
        result = sgManager.readSerialNumber(szSerialNumber);
        String SN = new String (szSerialNumber);
        debugMessage("ReadSerialNumber() ret: " + result + " ["	+ new String(szSerialNumber) + "]\n");

/*
 *  No longer used
 *
	    Bitmap b = Bitmap.createBitmap(mImageWidth,mImageHeight, Bitmap.Config.ARGB_8888);
	    b.setHasAlpha(false);
	    int[] intbuffer = new int[mImageWidth*mImageHeight];
	    for (int i=0; i<intbuffer.length; ++i)
	    	intbuffer[i] = (int) buffer[i];
	    b.setPixels(intbuffer, 0, mImageWidth, 0, 0, mImageWidth, mImageHeight);
	    mImageViewFingerprint.setImageBitmap(this.toGrayscale(b));
*/

        mImageViewFingerprint.setImageBitmap(sgManager.toGrayscale(buffer, mImageWidth, mImageHeight));

        buffer = null;
        szSerialNumber = null;
        SN = null;
    }

    public Handler fingerDetectedHandler = new Handler(){
        // @Override
        public void handleMessage(Message msg) {
            //Handle the message
            CaptureFingerPrint();
            if (mAutoOnEnabled) {
                mToggleButtonAutoOn.toggle();
                EnableControls();
            }
        }
    };

    public void EnableControls(){
        this.mCapture.setClickable(true);
        this.mCapture.setTextColor(getResources().getColor(android.R.color.white));
        this.mButtonRegister.setClickable(true);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.white));
        this.mButtonMatch.setClickable(true);
        this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.white));
    }

    public void DisableControls(){
        this.mCapture.setClickable(false);
        this.mCapture.setTextColor(getResources().getColor(android.R.color.black));
        this.mButtonRegister.setClickable(false);
        this.mButtonRegister.setTextColor(getResources().getColor(android.R.color.black));
        this.mButtonMatch.setClickable(false);
        this.mButtonMatch.setTextColor(getResources().getColor(android.R.color.black));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause()");

        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
        sgManager.stop();

        EnableControls();

        getActivity().unregisterReceiver(mUsbReceiver);
        mRegisterImage = null;
        mVerifyImage = null;
        mRegisterTemplate = null;
        mVerifyTemplate = null;
        mImageViewFingerprint.setImageBitmap(grayBitmap);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        getActivity().registerReceiver(mUsbReceiver, filter);

        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());

        long error = sgManager.init();
        debugMessage("jnisgfplib init: " + error);
        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
            else
                dlgAlert.setMessage("Fingerprint device initialization failed!");
            dlgAlert.setTitle("SecuGen Fingerprint SDK");
            dlgAlert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int whichButton){
        		        	getActivity().finish();
                            return;
                        }
                    }
            );
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        } else {
            UsbDevice usbDevice = sgManager.getUsbDevice();
            if (usbDevice == null){
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(getActivity());
                dlgAlert.setMessage("SDU04P or SDU03P fingerprint sensor not found!");
                dlgAlert.setTitle("SecuGen Fingerprint SDK");
                dlgAlert.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton){
                                getActivity().finish();
                                return;
                            }
                        }
                );
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            } else {
                error = sgManager.openDevice();
                debugMessage("OpenDevice() ret: " + error + "\n");
                SGDeviceInfoParam deviceInfo = new SGDeviceInfoParam();
                error = sgManager.getDeviceInfo(deviceInfo);
                debugMessage("GetDeviceInfo() ret: " + error + "\n");
                if (error == SGFDxErrorCode.SGFDX_ERROR_NONE) {
                    mImageWidth = deviceInfo.imageWidth;
                    mImageHeight = deviceInfo.imageHeight;

                    sgManager.setTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
                    sgManager.getMaxTemplateSize(mMaxTemplateSize);
                    debugMessage("TEMPLATE_FORMAT_SG400 SIZE: " + mMaxTemplateSize[0] + "\n");
                    mRegisterTemplate = new byte[mMaxTemplateSize[0]];
                    mVerifyTemplate = new byte[mMaxTemplateSize[0]];

                    sgManager.setSmartCapture(this.mToggleButtonSmartCapture.isChecked());

                    if (mAutoOnEnabled) {
                        sgManager.startAutoOn();
                        DisableControls();
                    }
                }
                //Thread thread = new Thread(this);
                //thread.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");

        SecuGenManager sgManager  = SecuGenManager.getInstance(getActivity());
        sgManager.close();

        mRegisterImage = null;
        mVerifyImage = null;
        mRegisterTemplate = null;
        mVerifyTemplate = null;

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // init views
        mCapture = view.findViewById(R.id.btnCapture);
        mButtonRegister = view.findViewById(R.id.btnRegister);
        mButtonMatch = view.findViewById(R.id.btnMatch);
        mButtonLed = view.findViewById(R.id.btnRed);
        mTextViewResult = view.findViewById(R.id.txtCheckResult);
        mCheckBoxMatched = view.findViewById(R.id.cbMatched);
        mToggleButtonSmartCapture = view.findViewById(R.id.switchSmartCap);
        mToggleButtonCaptureModeN = view.findViewById(R.id.switchCaptureModeN);
        mToggleButtonAutoOn = view.findViewById(R.id.switchAuto);
        mImageViewFingerprint = view.findViewById(R.id.imgFingerprintView);
        mImageViewRegister = view.findViewById(R.id.imgRegister);
        mImageViewVerify = view.findViewById(R.id.imgVerify);

        mCapture.setOnClickListener(this);
        mButtonRegister.setOnClickListener(this);
        mButtonMatch.setOnClickListener(this);
        mButtonLed.setOnClickListener(this);
        mToggleButtonSmartCapture.setOnClickListener(this);
        mToggleButtonCaptureModeN.setOnClickListener(this);
        mToggleButtonAutoOn.setOnClickListener(this);

        grayBuffer = new int[JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES*JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES];
        for (int i=0; i<grayBuffer.length; ++i)
            grayBuffer[i] = Color.GRAY;
        grayBitmap = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES, Bitmap.Config.ARGB_8888);
        grayBitmap.setPixels(grayBuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES);
        mImageViewFingerprint.setImageBitmap(grayBitmap);

        int[] sintbuffer = new int[(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2)*(JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2)];
        for (int i=0; i<sintbuffer.length; ++i)
            sintbuffer[i] = Color.GRAY;
        Bitmap sb = Bitmap.createBitmap(JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2, Bitmap.Config.ARGB_8888);
        sb.setPixels(sintbuffer, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, 0, 0, JSGFPLib.MAX_IMAGE_WIDTH_ALL_DEVICES/2, JSGFPLib.MAX_IMAGE_HEIGHT_ALL_DEVICES/2);
        mImageViewRegister.setImageBitmap(grayBitmap);
        mImageViewVerify.setImageBitmap(grayBitmap);

        mMaxTemplateSize = new int[1];

        //USB Permissions
        filter = new IntentFilter(ACTION_USB_PERMISSION);
        getActivity().registerReceiver(mUsbReceiver, filter);

        mLed = false;
        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
        sgManager.createSGAutoOnEventNotifier(this);
        debugMessage("jnisgfplib version: " + sgManager.getVersion() + "\n");
        mAutoOnEnabled = false;
        nCaptureModeN = 0;

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        long dwTimeStart = 0, dwTimeEnd = 0, dwTimeElapsed = 0;
        if (view.equals(mToggleButtonSmartCapture)) {
            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            sgManager.setSmartCapture(mToggleButtonSmartCapture.isChecked());
        } else if (view.equals(mToggleButtonCaptureModeN)) {
            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            sgManager.setSmartCaptureModeN(mToggleButtonCaptureModeN.isChecked());
        } else if (view.equals(mCapture)) {
            CaptureFingerPrint();
        } else if (view.equals(mToggleButtonAutoOn)) {
            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            if(mToggleButtonAutoOn.isChecked()) {
                mAutoOnEnabled = true;
                sgManager.startAutoOn();
                DisableControls();
            } else {
                mAutoOnEnabled = false;
                sgManager.stopAutoOn();
                EnableControls();
            }
        } else if (view.equals(mButtonLed)) {
            this.mCheckBoxMatched.setChecked(false);
            mLed = !mLed;
            dwTimeStart = System.currentTimeMillis();
            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            long result = sgManager.setLedOn(mLed);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("setLedOn(" + mLed +") ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mTextViewResult.setText("setLedOn(" + mLed +") ret: " + result + " [" + dwTimeElapsed + "ms]\n");
        } else if (view.equals(mButtonRegister)) {
            //DEBUG Log.d(TAG, "Clicked REGISTER");
            debugMessage("Clicked REGISTER\n");
            if (mRegisterImage != null)
                mRegisterImage = null;
            mRegisterImage = new byte[mImageWidth*mImageHeight];

            this.mCheckBoxMatched.setChecked(false);
            ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
            dwTimeStart = System.currentTimeMillis();

            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            long result = sgManager.getImage(mRegisterImage);
            sgManager.dumpFile("register.raw", mRegisterImage);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mImageViewFingerprint.setImageBitmap(sgManager.toGrayscale(mRegisterImage, mImageWidth, mImageHeight));
            dwTimeStart = System.currentTimeMillis();
            result = sgManager.setTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("SetTemplateFormat(SG400) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");
            SGFingerInfo fpInfo = new SGFingerInfo();
            for (int i=0; i< mRegisterTemplate.length; ++i)
                mRegisterTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgManager.createTemplate(fpInfo, mRegisterImage, mRegisterTemplate);
            sgManager.dumpFile("register.min", mRegisterTemplate);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("CreateTemplate() ret:" + result + " [" + dwTimeElapsed + "ms]\n");
            mImageViewRegister.setImageBitmap(sgManager.toGrayscale(mRegisterImage, mImageWidth, mImageHeight));
            mTextViewResult.setText("Click Verify");
            byteBuf = null;
            mRegisterImage = null;
            fpInfo = null;
        } else if (view.equals(mButtonMatch)) {
            //DEBUG Log.d(TAG, "Clicked MATCH");
            debugMessage("Clicked MATCH\n");
            if (mVerifyImage != null)
                mVerifyImage = null;
            mVerifyImage = new byte[mImageWidth*mImageHeight];
            ByteBuffer byteBuf = ByteBuffer.allocate(mImageWidth*mImageHeight);
            dwTimeStart = System.currentTimeMillis();

            SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
            long result = sgManager.getImage(mVerifyImage);
            sgManager.dumpFile("verify.raw", mVerifyImage);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("GetImage() ret:" + result + " [" + dwTimeElapsed + "ms]\n");

            mImageViewFingerprint.setImageBitmap(sgManager.toGrayscale(mVerifyImage, mImageWidth, mImageHeight));
            mImageViewVerify.setImageBitmap(sgManager.toGrayscale(mVerifyImage, mImageWidth, mImageHeight));
            dwTimeStart = System.currentTimeMillis();
            result = sgManager.setTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("SetTemplateFormat(SG400) ret:" +  result + " [" + dwTimeElapsed + "ms]\n");
            SGFingerInfo fpInfo = new SGFingerInfo();
            for (int i=0; i< mVerifyTemplate.length; ++i)
                mVerifyTemplate[i] = 0;
            dwTimeStart = System.currentTimeMillis();
            result = sgManager.createTemplate(fpInfo, mVerifyImage, mVerifyTemplate);
            sgManager.dumpFile("verify.min", mVerifyTemplate);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("CreateTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");
            boolean[] matched = new boolean[1];
            dwTimeStart = System.currentTimeMillis();
            sgManager.matchTemplate(mRegisterTemplate, mVerifyTemplate, SGFDxSecurityLevel.SL_NORMAL, matched);
            dwTimeEnd = System.currentTimeMillis();
            dwTimeElapsed = dwTimeEnd-dwTimeStart;
            debugMessage("MatchTemplate() ret:" + result+ " [" + dwTimeElapsed + "ms]\n");
            if (matched[0]) {
                mTextViewResult.setText("MATCHED!!\n");
                this.mCheckBoxMatched.setChecked(true);
                debugMessage("MATCHED!!\n");
            }
            else {
                mTextViewResult.setText("NOT MATCHED!!");
                this.mCheckBoxMatched.setChecked(false);
                debugMessage("NOT MATCHED!!\n");
            }
            byteBuf = null;
            mVerifyImage = null;
            fpInfo = null;
            matched = null;
        }
    }

    @Override
    public void SGFingerPresentCallback() {
        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
        sgManager.stopAutoOn();
        fingerDetectedHandler.sendMessage(new Message());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
