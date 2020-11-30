package com.mybluetooth.demofinger;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;
    OnResponse onResponse;

    public FingerPrintHandler(Context context, OnResponse onResponse) {
        this.context = context;
        this.onResponse = onResponse;
    }

    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject,cancellationSignal,0,this,null);
    }


    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("Error "+errString,false);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Failed To Authenticate",false);
    }

    public void update(String s, boolean b) {
        if(b){
            onResponse.clickResponse(true,"Success");
        }else{
            onResponse.clickResponse(false,s);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error "+helpString,false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Success",true);
    }

    public interface OnResponse{
        void clickResponse(boolean value,String string);
    }
}
