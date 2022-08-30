package com.carloshoil.taxiliteconductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.carloshoil.taxiliteconductor.Dialog.DialogoCarga;
import com.carloshoil.taxiliteconductor.Global.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistroCuentaActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText edUsuario, edPassWord, edPassWordConfir;
    Button btnCreaCuenta;
    DialogoCarga dialogoCarga;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_cuenta);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Init();

    }

    private void Init() {
        databaseReference= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        edUsuario=findViewById(R.id.edUsuario);
        edPassWord=findViewById(R.id.edPassWord);
        edPassWordConfir=findViewById(R.id.edPassWordConfirm);
        btnCreaCuenta=findViewById(R.id.btnCrearCuenta);
        btnCreaCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creaCuenta();
            }
        });
    }
    private void creaCuenta() {
        String cUsuario= edUsuario.getText().toString();
        String cPassWord=edPassWord.getText().toString();
        String cPassWordConfirm=edPassWordConfir.getText().toString();
        if(ValidaDatos(cUsuario, cPassWord, cPassWordConfirm))
        {
            MostrarDialogoCarga();
            auth.createUserWithEmailAndPassword(cUsuario, cPassWord).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        OcultarDialogoCarga();
                        Intent i= new Intent(RegistroCuentaActivity.this, RegistroDatosActivity.class);
                        Utilities.GuardarPreferencias("cEstatus", "RD", RegistroCuentaActivity.this);
                        Utilities.GuardarPreferencias("cClaveUsuario", task.getResult().getUser()==null?"":task.getResult().getUser().getUid(), RegistroCuentaActivity.this);
                        Utilities.GuardarPreferencias("cCorreo",cUsuario, RegistroCuentaActivity.this);
                        Toast.makeText(RegistroCuentaActivity.this, "¡Se ha creado correctamente la cuenta!", Toast.LENGTH_SHORT).show();
                        startActivity(i);
                        finish();

                    }
                    else
                    {
                        OcultarDialogoCarga();
                        String cMensajeToast="";
                        String cMensaje=task.getException().getMessage();
                        cMensajeToast="Error al crear cuenta, intente de nuevo";
                        if(cMensaje.equals("The email address is already in use by another account."))
                        {
                            cMensajeToast="La cuenta del correo: "+ cUsuario+ " ya ha sido usado, intente con otro por favor";
                        }
                        Toast.makeText(RegistroCuentaActivity.this, cMensajeToast, Toast.LENGTH_SHORT).show();
                        Log.d("DEBUG", task.getException().getMessage());
                    }
                }
            });
        }
    }
    private boolean ValidaDatos(String cNombreUsuario, String cPassWord, String cPassWordConfirm)
    {

        if(cNombreUsuario.trim().isEmpty()|| cPassWord.trim().isEmpty()||cPassWordConfirm.trim().isEmpty())
        {
            Toast.makeText(this, "Por favor, rellene todos los datos", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!cPassWord.trim().equals(cPassWordConfirm.trim()))
        {
            Toast.makeText(this, "Las contraseñas no coiciden, verifica por favor", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(cPassWord.trim().length()<6)
        {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!cNombreUsuario.contains("@")||!cNombreUsuario.contains(".com"))
        {
            Toast.makeText(this, "Error, ingresa un correo con el formato correcto", Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;
    }
    private void MostrarDialogoCarga()
    {
        dialogoCarga= new DialogoCarga(this);
        dialogoCarga.setCancelable(false);
        dialogoCarga.show(getSupportFragmentManager(),"dialogocarga");

    }
    private void OcultarDialogoCarga()
    {
        if(dialogoCarga!=null)
            dialogoCarga.dismiss();
    }
}