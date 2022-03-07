package com.debugger.ocr.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.debugger.ocr.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGallery = findViewById(R.id.btn_gallery);
        Button btnDigital = findViewById(R.id.btn_digital);
        Button btnPhysical = findViewById(R.id.btn_physical);

        btnGallery.setOnClickListener(v -> checkGalleryPermission());

        btnDigital.setOnClickListener(v -> checkCameraPermission());

        btnPhysical.setOnClickListener(v -> checkCameraPhysicalPermission());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGalleryActivity();
            }
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchCameraActivity();
            }
        }else if (requestCode == 3) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchCameraPhysicalActivity();
            }
        }

    }

    private void checkGalleryPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            switchGalleryActivity();
        }
    }

    private void switchGalleryActivity() {
        Intent i = new Intent(MainActivity.this, GalleryActivity.class);
        startActivity(i);
    }

    private void checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        } else {
            switchCameraActivity();
        }
    }

    private void switchCameraActivity() {
        Intent i = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(i);
    }

    private void checkCameraPhysicalPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
        } else {
            switchCameraPhysicalActivity();
        }
    }

    private void switchCameraPhysicalActivity() {
        Intent i = new Intent(MainActivity.this, CameraPhysicalActivity.class);
        startActivity(i);
    }



}