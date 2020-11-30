package com.mybluetooth.demofinger;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements FingerPrintHandler.OnResponse {

    ImageView imageView;
    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv_finger);
        checkSelfPer();
    }

    private void checkSelfPer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "No Finger Print or Scanner Found !!!", Toast.LENGTH_SHORT).show();
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission No Grated to Use Scannner", Toast.LENGTH_SHORT).show();
            } else if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "Add Lock to App", Toast.LENGTH_SHORT).show();
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "No Finger Addded to Phone", Toast.LENGTH_SHORT).show();
            } else {
                FingerPrintHandler handler = new FingerPrintHandler(this, this);
                handler.startAuth(fingerprintManager, null);
            }
        }
    }

    @Override
    public void clickResponse(boolean value, String re) {
        if (value) {
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Try Again " + re, Toast.LENGTH_SHORT).show();
        }
    }
}