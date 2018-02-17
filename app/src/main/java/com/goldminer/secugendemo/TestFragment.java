package com.goldminer.secugendemo;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGANSITemplateInfo;
import SecuGen.FDxSDKPro.SGFDxSecurityLevel;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import SecuGen.FDxSDKPro.SGFingerInfo;
import SecuGen.FDxSDKPro.SGISOTemplateInfo;
import SecuGen.FDxSDKPro.SGImpressionType;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1      = "param1";
    private static final String ARG_PARAM2      = "param2";

    private Button      mBtnSDKTest             = null;
    private TextView    mTxtTestResult          = null;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance(String param1, String param2) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_test, container, false);

        mBtnSDKTest = view.findViewById(R.id.btnSDKTest);
        mTxtTestResult = view.findViewById(R.id.txtTestResult);
        mTxtTestResult.setEnabled(false);

        mBtnSDKTest.setOnClickListener(this);

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
        if(view.equals(mBtnSDKTest)) {
            SDKTest();
        }
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

    private void debugMessage(String message) {
        this.mTxtTestResult.append(message);
        this.mTxtTestResult.invalidate();
    }

    private void SDKTest(){
        mTxtTestResult.setText("");
        debugMessage("\n###############\n");
        debugMessage("### SDK Test  ###\n");
        debugMessage("###############\n");

        int X_SIZE = 248;
        int Y_SIZE = 292;

        long error = 0;
        byte[] sgTemplate1;
        byte[] sgTemplate2;
        byte[] sgTemplate3;
        byte[] ansiTemplate1;
        byte[] ansiTemplate2;
        byte[] isoTemplate1;
        byte[] isoTemplate2;

        int[] size = new int[1];
        int[] score = new int[1];
        int[] quality1 = new int[1];
        int[] quality2 = new int[1];
        int[] quality3 = new int[1];
        boolean[] matched = new boolean[1];

        byte[] finger1 = new byte[X_SIZE*Y_SIZE];
        byte[] finger2 = new byte[X_SIZE*Y_SIZE];
        byte[] finger3 = new byte[X_SIZE*Y_SIZE];
        try {
            InputStream fileInputStream =getResources().openRawResource(R.raw.finger_0_10_3);
            error = fileInputStream.read(finger1);
            fileInputStream.close();
        } catch (IOException ex){
            debugMessage("Error: Unable to find fingerprint image R.raw.finger_0_10_3.\n");
            return;
        }
        try {
            InputStream fileInputStream =getResources().openRawResource(R.raw.finger_1_10_3);
            error = fileInputStream.read(finger2);
            fileInputStream.close();
        } catch (IOException ex){
            debugMessage("Error: Unable to find fingerprint image R.raw.finger_1_10_3.\n");
            return;
        }
        try {
            InputStream fileInputStream =getResources().openRawResource(R.raw.finger_2_10_3);
            error = fileInputStream.read(finger3);
            fileInputStream.close();
        } catch (IOException ex){
            debugMessage("Error: Unable to find fingerprint image R.raw.finger_2_10_3.\n");
            return;
        }

        SecuGenManager sgManager = SecuGenManager.getInstance(getActivity());
        JSGFPLib sgFplibSDKTest = sgManager.getJSGFPLib();

        if(sgFplibSDKTest == null)
            return;

        error = sgFplibSDKTest.InitEx( X_SIZE, Y_SIZE, 500);
        debugMessage("InitEx("+ X_SIZE + "," + Y_SIZE + ",500) ret:" +  error + "\n");

        SGFingerInfo fpInfo1 = new SGFingerInfo();
        SGFingerInfo fpInfo2 = new SGFingerInfo();
        SGFingerInfo fpInfo3 = new SGFingerInfo();

        error = sgFplibSDKTest.GetImageQuality((long)X_SIZE, (long)Y_SIZE, finger1, quality1);
        debugMessage("GetImageQuality(R.raw.finger_0_10_3) ret:" +  error + " Finger quality=" +  quality1[0] + "\n");
        error = sgFplibSDKTest.GetImageQuality((long)X_SIZE, (long)Y_SIZE, finger2, quality2);
        debugMessage("GetImageQuality(R.raw.finger_1_10_3) ret:" +  error + " Finger quality=" +  quality2[0] + "\n");
        error = sgFplibSDKTest.GetImageQuality((long)X_SIZE, (long)Y_SIZE, finger3, quality3);
        debugMessage("GetImageQuality(R.raw.finger_2_10_3) ret:" +  error + " Finger quality=" +  quality3[0] + "\n");

        fpInfo1.FingerNumber = 1;
        fpInfo1.ImageQuality = quality1[0];
        fpInfo1.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fpInfo1.ViewNumber = 1;

        fpInfo2.FingerNumber = 1;
        fpInfo2.ImageQuality = quality2[0];
        fpInfo2.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fpInfo2.ViewNumber = 2;

        fpInfo3.FingerNumber = 1;
        fpInfo3.ImageQuality = quality3[0];
        fpInfo3.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fpInfo3.ViewNumber = 3;



        ///////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////        
        //TEST SG400
        debugMessage("#######################\n");
        debugMessage("TEST SG400\n");
        debugMessage("###\n###\n");
        error = sgFplibSDKTest.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400);
        debugMessage("SetTemplateFormat(SG400) ret:" +  error + "\n");
        error = sgFplibSDKTest.GetMaxTemplateSize(size);
        debugMessage("GetMaxTemplateSize() ret:" +  error + " SG400_MAX_SIZE=" +  size[0] + "\n");

        sgTemplate1  = new byte[size[0]];
        sgTemplate2 = new byte[size[0]];
        sgTemplate3 = new byte[size[0]];

        //TEST DeviceInfo

        error = sgFplibSDKTest.CreateTemplate(null, finger1, sgTemplate1);
        debugMessage("CreateTemplate(finger3) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(sgTemplate1, size);
        debugMessage("GetTemplateSize() ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.CreateTemplate(null, finger2, sgTemplate2);
        debugMessage("CreateTemplate(finger2) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(sgTemplate2, size);
        debugMessage("GetTemplateSize() ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.CreateTemplate(null, finger3, sgTemplate3);
        debugMessage("CreateTemplate(finger3) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(sgTemplate3, size);
        debugMessage("GetTemplateSize() ret:" +  error + " size=" +  size[0] + "\n");

        ///////////////////////////////////////////////////////////////////////////////////////////////
        error = sgFplibSDKTest.MatchTemplate(sgTemplate1, sgTemplate2, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchTemplate(sgTemplate1,sgTemplate2) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetMatchingScore(sgTemplate1, sgTemplate2,  score);
        debugMessage("GetMatchingScore(sgTemplate1, sgTemplate2) ret:" + error + ". Score:" + score[0] + "\n");


        ///////////////////////////////////////////////////////////////////////////////////////////////
        error = sgFplibSDKTest.MatchTemplate(sgTemplate1, sgTemplate3, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchTemplate(sgTemplate1,sgTemplate3) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetMatchingScore(sgTemplate1, sgTemplate3,  score);
        debugMessage("GetMatchingScore(sgTemplate1, sgTemplate3) ret:" + error + ". Score:" + score[0] + "\n");


        ///////////////////////////////////////////////////////////////////////////////////////////////
        error = sgFplibSDKTest.MatchTemplate(sgTemplate2, sgTemplate3, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchTemplate(sgTemplate2,sgTemplate3) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetMatchingScore(sgTemplate2, sgTemplate3,  score);
        debugMessage("GetMatchingScore(sgTemplate2, sgTemplate3) ret:" + error + ". Score:" + score[0] + "\n");


        ///////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////        
        //TEST ANSI378
        debugMessage("#######################\n");
        debugMessage("TEST ANSI378\n");
        debugMessage("###\n###\n");
        error = sgFplibSDKTest.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ANSI378);
        debugMessage("SetTemplateFormat(ANSI378) ret:" +  error + "\n");
        error = sgFplibSDKTest.GetMaxTemplateSize(size);
        debugMessage("GetMaxTemplateSize() ret:" +  error + " ANSI378_MAX_SIZE=" +  size[0] + "\n");

        ansiTemplate1  = new byte[size[0]];
        ansiTemplate2 = new byte[size[0]];

        error = sgFplibSDKTest.CreateTemplate(fpInfo1, finger1, ansiTemplate1);
        debugMessage("CreateTemplate(finger1) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(ansiTemplate1, size);
        debugMessage("GetTemplateSize(ansi) ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.CreateTemplate(fpInfo2, finger2, ansiTemplate2);
        debugMessage("CreateTemplate(finger2) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(ansiTemplate2, size);
        debugMessage("GetTemplateSize(ansi) ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.MatchTemplate(ansiTemplate1, ansiTemplate2, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchTemplate(ansi) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetMatchingScore(ansiTemplate1, ansiTemplate2,  score);
        debugMessage("GetMatchingScore(ansi) ret:" + error + ". Score:" + score[0] + "\n");

        error = sgFplibSDKTest.GetTemplateSizeAfterMerge(ansiTemplate1, ansiTemplate2, size);
        debugMessage("GetTemplateSizeAfterMerge(ansi) ret:" + error + ". Size:" + size[0] + "\n");

        byte[] mergedAnsiTemplate1 = new byte[size[0]];
        error = sgFplibSDKTest.MergeAnsiTemplate(ansiTemplate1, ansiTemplate2, mergedAnsiTemplate1);
        debugMessage("MergeAnsiTemplate() ret:" + error + "\n");

        error = sgFplibSDKTest.MatchAnsiTemplate(ansiTemplate1, 0, mergedAnsiTemplate1, 0, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchAnsiTemplate(0,0) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.MatchAnsiTemplate(ansiTemplate1, 0, mergedAnsiTemplate1, 1, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchAnsiTemplate(0,1) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetAnsiMatchingScore(ansiTemplate1, 0, mergedAnsiTemplate1, 0, score);
        debugMessage("GetAnsiMatchingScore(0,0) ret:" + error + ". Score:" + score[0] + "\n");

        error = sgFplibSDKTest.GetAnsiMatchingScore(ansiTemplate1, 0, mergedAnsiTemplate1, 1, score);
        debugMessage("GetAnsiMatchingScore(0,1) ret:" + error + ". Score:" + score[0] + "\n");

        SGANSITemplateInfo ansiTemplateInfo = new SGANSITemplateInfo();
        error = sgFplibSDKTest.GetAnsiTemplateInfo(ansiTemplate1, ansiTemplateInfo);
        debugMessage("GetAnsiTemplateInfo(ansiTemplate1) ret:" + error + "\n");
        debugMessage("   TotalSamples=" + ansiTemplateInfo.TotalSamples + "\n");
        for (int i=0; i<ansiTemplateInfo.TotalSamples; ++i){
            debugMessage("   Sample[" + i + "].FingerNumber=" + ansiTemplateInfo.SampleInfo[i].FingerNumber + "\n");
            debugMessage("   Sample[" + i + "].ImageQuality=" + ansiTemplateInfo.SampleInfo[i].ImageQuality + "\n");
            debugMessage("   Sample[" + i + "].ImpressionType=" + ansiTemplateInfo.SampleInfo[i].ImpressionType + "\n");
            debugMessage("   Sample[" + i + "].ViewNumber=" + ansiTemplateInfo.SampleInfo[i].ViewNumber + "\n");
        }

        error = sgFplibSDKTest.GetAnsiTemplateInfo(mergedAnsiTemplate1, ansiTemplateInfo);
        debugMessage("GetAnsiTemplateInfo(mergedAnsiTemplate1) ret:" + error + "\n");
        debugMessage("   TotalSamples=" + ansiTemplateInfo.TotalSamples + "\n");

        for (int i=0; i<ansiTemplateInfo.TotalSamples; ++i){
            debugMessage("   Sample[" + i + "].FingerNumber=" + ansiTemplateInfo.SampleInfo[i].FingerNumber + "\n");
            debugMessage("   Sample[" + i + "].ImageQuality=" + ansiTemplateInfo.SampleInfo[i].ImageQuality + "\n");
            debugMessage("   Sample[" + i + "].ImpressionType=" + ansiTemplateInfo.SampleInfo[i].ImpressionType + "\n");
            debugMessage("   Sample[" + i + "].ViewNumber=" + ansiTemplateInfo.SampleInfo[i].ViewNumber + "\n");
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////////        
        //TEST ISO19794-2
        debugMessage("#######################\n");
        debugMessage("TEST ISO19794-2\n");
        debugMessage("###\n###\n");
        error = sgFplibSDKTest.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        debugMessage("SetTemplateFormat(ISO19794) ret:" +  error + "\n");
        error = sgFplibSDKTest.GetMaxTemplateSize(size);
        debugMessage("GetMaxTemplateSize() ret:" +  error + " ISO19794_MAX_SIZE=" +  size[0] + "\n");

        isoTemplate1  = new byte[size[0]];
        isoTemplate2 = new byte[size[0]];

        error = sgFplibSDKTest.CreateTemplate(fpInfo1, finger1, isoTemplate1);
        debugMessage("CreateTemplate(finger1) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(isoTemplate1, size);
        debugMessage("GetTemplateSize(iso) ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.CreateTemplate(fpInfo2, finger2, isoTemplate2);
        debugMessage("CreateTemplate(finger2) ret:" + error + "\n");
        error = sgFplibSDKTest.GetTemplateSize(isoTemplate2, size);
        debugMessage("GetTemplateSize(iso) ret:" +  error + " size=" +  size[0] + "\n");

        error = sgFplibSDKTest.MatchTemplate(isoTemplate1, isoTemplate2, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchTemplate(iso) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetMatchingScore(isoTemplate1, isoTemplate2,  score);
        debugMessage("GetMatchingScore(iso) ret:" + error + ". Score:" + score[0] + "\n");

        error = sgFplibSDKTest.GetIsoTemplateSizeAfterMerge(isoTemplate1, isoTemplate2, size);
        debugMessage("GetIsoTemplateSizeAfterMerge() ret:" + error + ". Size:" + size[0] + "\n");


        byte[] mergedIsoTemplate1 = new byte[size[0]];
        error = sgFplibSDKTest.MergeIsoTemplate(isoTemplate1, isoTemplate2, mergedIsoTemplate1);
        debugMessage("MergeIsoTemplate() ret:" + error + "\n");

        error = sgFplibSDKTest.MatchIsoTemplate(isoTemplate1, 0, mergedIsoTemplate1, 0, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchIsoTemplate(0,0) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.MatchIsoTemplate(isoTemplate1, 0, mergedIsoTemplate1, 1, SGFDxSecurityLevel.SL_NORMAL, matched);
        debugMessage("MatchIsoTemplate(0,1) ret:" + error + "\n");
        if (matched[0])
            debugMessage("MATCHED!!\n");
        else
            debugMessage("NOT MATCHED!!\n");

        error = sgFplibSDKTest.GetIsoMatchingScore(isoTemplate1, 0, mergedIsoTemplate1, 0, score);
        debugMessage("GetIsoMatchingScore(0,0) ret:" + error + ". Score:" + score[0] + "\n");

        error = sgFplibSDKTest.GetIsoMatchingScore(isoTemplate1, 0, mergedIsoTemplate1, 1, score);
        debugMessage("GetIsoMatchingScore(0,1) ret:" + error + ". Score:" + score[0] + "\n");

        SGISOTemplateInfo isoTemplateInfo = new SGISOTemplateInfo();
        error = sgFplibSDKTest.GetIsoTemplateInfo(isoTemplate1, isoTemplateInfo);
        debugMessage("GetIsoTemplateInfo(isoTemplate1) ret:" + error + "\n");
        debugMessage("   TotalSamples=" + isoTemplateInfo.TotalSamples + "\n");
        for (int i=0; i<isoTemplateInfo.TotalSamples; ++i){
            debugMessage("   Sample[" + i + "].FingerNumber=" + isoTemplateInfo.SampleInfo[i].FingerNumber + "\n");
            debugMessage("   Sample[" + i + "].ImageQuality=" + isoTemplateInfo.SampleInfo[i].ImageQuality + "\n");
            debugMessage("   Sample[" + i + "].ImpressionType=" + isoTemplateInfo.SampleInfo[i].ImpressionType + "\n");
            debugMessage("   Sample[" + i + "].ViewNumber=" + isoTemplateInfo.SampleInfo[i].ViewNumber + "\n");
        }

        error = sgFplibSDKTest.GetIsoTemplateInfo(mergedIsoTemplate1, isoTemplateInfo);
        debugMessage("GetIsoTemplateInfo(mergedIsoTemplate1) ret:" + error + "\n");
        debugMessage("   TotalSamples=" + isoTemplateInfo.TotalSamples + "\n");
        for (int i=0; i<isoTemplateInfo.TotalSamples; ++i){
            debugMessage("   Sample[" + i + "].FingerNumber=" + isoTemplateInfo.SampleInfo[i].FingerNumber + "\n");
            debugMessage("   Sample[" + i + "].ImageQuality=" + isoTemplateInfo.SampleInfo[i].ImageQuality + "\n");
            debugMessage("   Sample[" + i + "].ImpressionType=" + isoTemplateInfo.SampleInfo[i].ImpressionType + "\n");
            debugMessage("   Sample[" + i + "].ViewNumber=" + isoTemplateInfo.SampleInfo[i].ViewNumber + "\n");
        }


        //Reset extractor/matcher for attached device opened in resume() method
        error = sgFplibSDKTest.InitEx( X_SIZE, Y_SIZE, 500);
        debugMessage("InitEx("+ X_SIZE + "," + Y_SIZE + ",500) ret:" +  error + "\n");

        debugMessage("\n## END SDK TEST ##\n");
    }
}
