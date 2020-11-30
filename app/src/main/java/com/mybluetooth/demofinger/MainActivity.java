package com.mybluetooth.demofinger;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/*
 Reference Video // https://www.youtube.com/watch?v=dI9TItdw83U&t=1870s
 Ankit Prajapati
 30/11/2020
 */
public class MainActivity extends AppCompatActivity implements FingerPrintHandler.OnResponse {

    ImageView imageView;
    FingerprintManager fingerprintManager;
    KeyguardManager keyguardManager;

    KeyStore keyStore;

    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";

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

                generateKey();

                try {
                    if (cipherInit()) {

                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        FingerPrintHandler fingerprintHandler = new FingerPrintHandler(this, this);
                        fingerprintHandler.startAuth(fingerprintManager, cryptoObject);

                    }
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
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


    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC).setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException | IOException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() throws NoSuchPaddingException {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | InvalidKeyException | UnrecoverableKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }
}