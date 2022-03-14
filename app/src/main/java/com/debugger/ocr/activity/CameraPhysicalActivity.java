package com.debugger.ocr.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.debugger.ocr.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraPhysicalActivity extends AppCompatActivity {

    private PreviewView cameraView;
    private TextView tvLabel;
    private TextView card_side;
    private LinearLayout point_result;
    private EditText etCardNo;
    private EditText etName;
    private EditText etAddress;
    private LinearLayout switch_side;
    private LinearLayout llButtons;
    private ImageView capture_btn;
    private ViewFinderView viewFinder_View;
    private static final String TAG = CameraPhysicalActivity.class.getName();

    Button bClear, bCopy, btn_front, btn_back;
    StringBuilder strBuilder1;
    Boolean check = true;
    int side_val = 0;

    String Card_NO = "";
    String fullName = "";
    String Address = "";

    private String scan_result="";
    private String[] scan_result_pl = {"",};

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = cameraProviderListenableFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                TextRecognizerOptions textRecognizerOptions = new TextRecognizerOptions.Builder().build();
                TextRecognizer textRecognizer = TextRecognition.getClient(textRecognizerOptions);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                imageAnalysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(), (analyzer) -> processImage(textRecognizer, analyzer)
                );

                processCameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImage(TextRecognizer textRecognizer, ImageProxy analyzer) {
        if (analyzer.getImage() != null) {
            InputImage inputImage = InputImage.fromMediaImage(analyzer.getImage(), analyzer.getImageInfo().getRotationDegrees());
            textRecognizer.process(inputImage)
                    .addOnSuccessListener(text -> processData(text.getTextBlocks()))
                    .addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e))
                    .addOnCompleteListener(task -> {
                        analyzer.getImage().close();
                        analyzer.close();
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_physical);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        cameraView = findViewById(R.id.surface_view);
        tvLabel = findViewById(R.id.tv_label);
        card_side = findViewById(R.id.card_side);
        point_result = findViewById(R.id.point_result);
        etCardNo = findViewById(R.id.et_card_no);
        etName = findViewById(R.id.et_name);
        etAddress = findViewById(R.id.et_address);
        bClear = findViewById(R.id.b_clear);
        bCopy = findViewById(R.id.b_copy);
        llButtons = findViewById(R.id.llButtons);
        capture_btn = findViewById(R.id.capture_btn);
        switch_side = findViewById(R.id.switch_side);
        btn_front = findViewById(R.id.btn_front);
        btn_back = findViewById(R.id.btn_back);

        viewFinder_View = new ViewFinderView(this);
        viewFinder_View.setFrameAspectRatio((float) 1.6, (float) 0.9);
        viewFinder_View.setFrameSize((float)0.85);
        viewFinder_View.setMaskColor(Color.parseColor("#77000000"));
        viewFinder_View.setFrameColor(Color.parseColor("#FFFFFF"));
        viewFinder_View.setFrameCornersSize(660);
        viewFinder_View.setFrameCornersRadius(30);
        viewFinder_View.setFrameThickness(2);
        WindowManager.LayoutParams viewfinder_layoutParams =
                new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        this.addContentView(viewFinder_View, viewfinder_layoutParams);

        bClear.setOnClickListener(v -> {
            etCardNo.setText("");
            etName.setText("");
            etAddress.setText("");
        });

        if(check){
            cameraView.setVisibility(View.VISIBLE);
            viewFinder_View.setVisibility(View.VISIBLE);
            capture_btn.setVisibility(View.VISIBLE);
            tvLabel.setVisibility(View.GONE);
            point_result.setVisibility(View.GONE);
            llButtons.setVisibility(View.GONE);
            switch_side.setVisibility(View.GONE);
        }

//        etCardNo.setOnKeyListener((view, i, keyEvent) -> {
//            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
//                cameraView.setVisibility(View.VISIBLE);
//                viewFinder_View.setVisibility(View.VISIBLE);
//                capture_btn.setVisibility(View.VISIBLE);
//                tvLabel.setVisibility(View.VISIBLE);
//                point_result.setVisibility(View.GONE);
//                llButtons.setVisibility(View.GONE);
//                switch_side.setVisibility(View.GONE);
//            }
//            return true;
//        });

        setupCamera();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void processData(List<Text.TextBlock> textBlocks) {
        final StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < textBlocks.size(); i++) {
            Text.TextBlock item = textBlocks.get(i);

            strBuilder.append(item.getText());
            strBuilder.append("/");

        }

        String str = strBuilder.toString();
        String str2 = str.replace("/", "\n");
        String[] str1 = str2.split("\n");
        strBuilder1 = new StringBuilder();

        for (String s : str1) {
            strBuilder1.append(s);
            strBuilder1.append("/");

        }

        tvLabel.post(() -> {
            String scan_builder= strBuilder1.toString().toUpperCase();

            if(side_val == 0){
                scan_result = "";
                if(scan_builder.indexOf("REPUBLIC") > 0)
                    scan_result = scan_builder.substring(scan_builder.indexOf("REPUBLIC"));
                else{
                    if(scan_builder.indexOf("C OF SINGA") > 0)
                        scan_result = scan_builder.substring(scan_builder.indexOf("C OF SINGA"));
                }
                if(scan_result.length() > 2){
                    viewFinder_View.setFrameColor(Color.parseColor("#02F868"));
                }
                else {
                    viewFinder_View.setFrameColor(Color.parseColor("#D50000"));
                }
                try{
                    scan_result_pl = scan_result.split("/");
                    Card_NO = scan_result_pl[1];
                    Card_NO = Card_NO.substring(Card_NO.length()-10, Card_NO.length());
                    fullName = scan_result_pl[3];
                    if(scan_result_pl[4].indexOf("(")>0 || scan_result_pl[4].indexOf(")")>0){
                        fullName = fullName + "\n" + scan_result_pl[4];
                    }

//                    if(scan_result.indexOf("CARD NO") > 0 && scan_result.indexOf("NAME") > scan_result.indexOf("CARD NO")){
//                        Card_NO = scan_result.substring(scan_result.indexOf("CARD NO")+9,scan_result.indexOf("NAME")-1);
//                        if(scan_result.indexOf("RACE") > scan_result.indexOf("NAME"))
//                            fullName = scan_result.substring(scan_result.indexOf("NAME")+5,scan_result.indexOf("RACE")-1);
//                    }
                }catch (Exception e){

                }
            }
            else{
                scan_result = "";
                if(scan_builder.indexOf("SINGAPORE") > 0)
                    scan_result = scan_builder;
                else{
                    if(scan_builder.indexOf("APORE") > 0)
                        scan_result = scan_builder;
                }
                if(scan_result.length() > 2){
                    viewFinder_View.setFrameColor(Color.parseColor("#02F868"));
                }
                else {
                    viewFinder_View.setFrameColor(Color.parseColor("#D50000"));
                }

                try{
                    scan_result_pl = scan_result.split("/");
                    for(int i=1;i<scan_result_pl.length;i++) {
                        if(scan_result_pl[i].indexOf("SINGAPORE")>=0  || scan_result_pl[i].indexOf("APORE")>=0){
                            if(scan_result_pl[i-2].length()>7)
                                Address = scan_result_pl[i-2] + "\n" + scan_result_pl[i-1] + "\n" + scan_result_pl[i];
                            else
                                Address = scan_result_pl[i-1] + "\n" + scan_result_pl[i];
                        }
                    }

//                    if(scan_result.indexOf("ADDRESS") > 0)
//                        Address = scan_result.substring(scan_result.indexOf("ADDRESS")+8);
//                    if(Address.indexOf("NRIC") > 0)
//                        Address = Address.substring(0, Address.indexOf("NRIC"));
                }catch (Exception e){

                }
            }

            tvLabel.setText(scan_result);
//            tvLabel.setOnClickListener(v -> {
            capture_btn.setOnClickListener(v -> {
                if(side_val == 0){
                    etCardNo.setText(Card_NO);
                    etName.setText(fullName.replace("/", "\n"));
                }
                else {
                    etAddress.setText(Address.replace("/", "\n"));
                }
                check = false;
                capture_btn.setVisibility(View.GONE);
                cameraView.setVisibility(View.GONE);
                viewFinder_View.setVisibility(View.GONE);
                tvLabel.setVisibility(View.GONE);
                point_result.setVisibility(View.VISIBLE);
                llButtons.setVisibility(View.VISIBLE);
                switch_side.setVisibility(View.VISIBLE);
            });

            btn_front.setOnClickListener(v -> {
                check = true;
                side_val = 0;
                Card_NO = "";
                fullName = "";
                card_side.setText(R.string.front_side);
                cameraView.setVisibility(View.VISIBLE);
                viewFinder_View.setVisibility(View.VISIBLE);
                capture_btn.setVisibility(View.VISIBLE);
                point_result.setVisibility(View.GONE);
                llButtons.setVisibility(View.GONE);
                switch_side.setVisibility(View.GONE);
            });

            btn_back.setOnClickListener(v -> {
                check = true;
                side_val = 1;
                Address = "";
                card_side.setText(R.string.back_side);
                cameraView.setVisibility(View.VISIBLE);
                viewFinder_View.setVisibility(View.VISIBLE);
                capture_btn.setVisibility(View.VISIBLE);
                point_result.setVisibility(View.GONE);
                llButtons.setVisibility(View.GONE);
                switch_side.setVisibility(View.GONE);
            });

            bCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", etCardNo.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(),"Copied Successfully", Toast.LENGTH_SHORT).show();
            });

        });
    }


}