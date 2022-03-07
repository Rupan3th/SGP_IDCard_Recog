package com.debugger.ocr.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

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

public class CameraActivity extends AppCompatActivity {

    private PreviewView cameraView;
    private TextView tvLabel;
    private LinearLayout point_Result;
    private TextView nric_no;
    private TextView nric_no_last4;
    private TextView full_name;
    private TextView address;
    private TextView post_code;
    private EditText etOutput;
    private LinearLayout llButtons;
    private ImageView capture_btn;
    private static final String TAG = CameraActivity.class.getName();
    Button bClear, bRegex, bCopy;
    StringBuilder strBuilder1;
    ScrollView scr;
    Boolean check = false;

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
        setContentView(R.layout.activity_camera);
        cameraView = findViewById(R.id.surface_view);
        tvLabel = findViewById(R.id.tv_label);
        point_Result = findViewById(R.id.point_result);
        nric_no = findViewById(R.id.nric_no);
        nric_no_last4 = findViewById(R.id.nric_no_last4);
        full_name = findViewById(R.id.full_name);
        address = findViewById(R.id.address);
        post_code = findViewById(R.id.post_code);
        etOutput = findViewById(R.id.et_output);
        bClear = findViewById(R.id.b_clear);
        bCopy = findViewById(R.id.b_copy);
        llButtons = findViewById(R.id.llButtons);
        bRegex = findViewById(R.id.b_regex);
        scr = findViewById(R.id.scroll_view);
        capture_btn = findViewById(R.id.capture_btn);

        bClear.setOnClickListener(v -> etOutput.setText(""));
        bRegex.setOnClickListener(v -> check = !check);
        etOutput.setOnKeyListener((view, i, keyEvent) -> {
            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                cameraView.setVisibility(View.VISIBLE);
                point_Result.setVisibility(View.VISIBLE);
                capture_btn.setVisibility(View.VISIBLE);
                tvLabel.setVisibility(View.VISIBLE);
                bRegex.setVisibility(View.INVISIBLE);
                etOutput.setVisibility(View.GONE);
                llButtons.setVisibility(View.GONE);
                scr.setVisibility(View.GONE);
            }
            return true;
        });

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

        //Regex Operation
        String str = strBuilder.toString();
        String str2 = str.replace("/", "\n");
        String[] str1 = str2.split("\n");
        strBuilder1 = new StringBuilder();

        for (String s : str1) {
            if (check) {
                if (s.replace("-", "").matches("^(\\+\\d{1,9}[- ]?)?\\d{10}$")) {
                    strBuilder1.append(s);
                    strBuilder1.append("/");
                }
            } else {
                strBuilder1.append(s);
                strBuilder1.append("/");
            }

        }

        tvLabel.post(() -> {
            String scan_result= strBuilder1.toString();
            try{
                if(scan_result.indexOf("Hide detail") > 0)
                    scan_result = scan_result.substring(0, scan_result.indexOf("Hide detail"));
                if(scan_result.indexOf("NAME") > 0)
                    scan_result = scan_result.substring(scan_result.indexOf("NAME"));

                String fullName = "";
                String NRIC_NO = "";
                if(scan_result.indexOf("NRIC") > 4){
                    fullName = scan_result.substring(5, scan_result.indexOf("NRIC")-1);
                    if(scan_result.indexOf("DATE OF BIR") > 0)
                        NRIC_NO = scan_result.substring(scan_result.indexOf("NRIC")+9, scan_result.indexOf("DATE OF BIR")-1);
                }
                full_name.setText(fullName.replace("/", "\n"));
                nric_no.setText(NRIC_NO);
                nric_no_last4.setText(NRIC_NO.substring(NRIC_NO.length()-4));
                String ADDRESS = "";
                String POSTCODE = "";

                if(scan_result.indexOf("ADDRESS") > 0 && scan_result.indexOf("SINGAPORE 8") > 9 && scan_result.indexOf("ADDRESS")+9<scan_result.indexOf("SINGAPORE 8")){
                    ADDRESS = scan_result.substring(scan_result.indexOf("ADDRESS")+8, scan_result.indexOf("SINGAPORE 8")-1);
                    POSTCODE = scan_result.substring(scan_result.indexOf("SINGAPORE 8")+10);
                }
                address.setText(ADDRESS.replace("/", "\n"));
                post_code.setText(POSTCODE.substring(0, 6));
            }catch (Exception e){

            }


//            tvLabel.setText(scan_result);
//            tvLabel.setOnClickListener(v -> {
            capture_btn.setOnClickListener(v -> {
                point_Result.setVisibility(View.GONE);
                capture_btn.setVisibility(View.GONE);
                cameraView.setVisibility(View.GONE);
                tvLabel.setVisibility(View.GONE);
                bRegex.setVisibility(View.GONE);
                etOutput.setVisibility(View.VISIBLE);
                llButtons.setVisibility(View.VISIBLE);
                scr.setVisibility(View.VISIBLE);

//                String all_number = tvLabel.getText().toString();
//                all_number = all_number.replace("/", "\n");

                String all_number = "NRIC NO.\n"+nric_no.getText().toString()+"\n"+
                                    nric_no_last4.getText().toString()+"\n"+ "\nName:\n"+
                                    full_name.getText().toString()+"\n"+ "\nAddress:\n"+
                                    address.getText().toString()+"\n"+ "\nPostal Code:\n"+
                                    post_code.getText().toString();

                etOutput.setText(all_number);
                etOutput.requestFocus();
            });
            bCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied", etOutput.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(),"Copied Successfully", Toast.LENGTH_SHORT).show();
            });

        });
    }

}

