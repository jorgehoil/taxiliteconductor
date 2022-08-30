package com.carloshoil.taxiliteconductor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.carloshoil.taxiliteconductor.Global.Utilities;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SplashActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        inicia();



    }
    public void inicia()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                String cEstatus= Utilities.RecuperaPreferencia("cEstatus",SplashActivity.this);
                switch (cEstatus) {
                    case "":
                        i = new Intent(SplashActivity.this, MainActivity.class);
                        break;
                    case "RD":
                        i = new Intent(SplashActivity.this, RegistroDatosActivity.class);
                        break;
                    case "COMP":
                        i = new Intent(SplashActivity.this, PrincipalActivity.class);
                        break;
                    default:
                        i = new Intent(SplashActivity.this, MainActivity.class);
                        break;
                }
                startActivity(i);
                finish();
            }
        }, 1500);
    }

}