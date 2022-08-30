package com.carloshoil.taxiliteconductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.carloshoil.taxiliteconductor.Dialog.DialogoCarga;
import com.carloshoil.taxiliteconductor.Global.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    EditText edCorreo, edPassWord;
    Button btnIniciarSesion;
    FirebaseAuth firebaseAuth;
    DialogoCarga dialogoCarga;
    TextView tvRegistrate;
    DatabaseReference databaseReferenceTaxistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Init();
    }

    private void Init() {
        databaseReferenceTaxistas= FirebaseDatabase.getInstance().getReference().child("taxistas");
        firebaseAuth = FirebaseAuth.getInstance();
        edCorreo = findViewById(R.id.edCorreo);
        tvRegistrate=findViewById(R.id.lbRegistrate);
        edPassWord = findViewById(R.id.edPass);
        btnIniciarSesion=findViewById(R.id.btnIniciarSesion);
        tvRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AbrirRegistroDatos();
            }
        });
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ValidaDatos()){
                    Toast.makeText(MainActivity.this, "Error, verifica que los campos esten llenos", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    IniciaSesion(edCorreo.getText().toString(), edPassWord.getText().toString());
                }
            }
        });
    }

    private void AbrirRegistroDatos() {
        Intent i= new Intent(MainActivity.this, RegistroCuentaActivity.class);
        startActivity(i);
        finish();
    }

    private boolean ValidaDatos() {
        return edCorreo.getText().toString().trim().isEmpty()||edPassWord.getText().toString().trim().isEmpty();
        
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
    private void IniciaSesion(String cCorreo, String cPassWord) {
        MostrarDialogoCarga();
        firebaseAuth.signInWithEmailAndPassword(cCorreo, cPassWord).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    if(!task.getResult().getUser().getUid().isEmpty())
                    {
                        RecuperaDatosUsuario(task.getResult().getUser().getUid());
                    }
                    else
                    {
                        OcultarDialogoCarga();
                        Utilities.MostrarMensaje(MainActivity.this,"Error","Se ha presentado un error al recuperar el id de usuario, comunícate con el desarrollador para más información");
                    }

                }
                else
                {
                    OcultarDialogoCarga();
                    Toast.makeText(MainActivity.this, "Por favor verifica tu correo y/o contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void RecuperaDatosUsuario(String cIdUsuario) {

        databaseReferenceTaxistas.child(cIdUsuario).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    OcultarDialogoCarga();
                   if(task.getResult()!=null)
                   {
                       Utilities.GuardarPreferencias("cEstatus", "COMP", MainActivity.this);
                       GuardaDatosUsuario(task.getResult());
                       Intent i= new Intent(MainActivity.this, PrincipalActivity.class);
                       startActivity(i);
                       finish();
                   }
                   else
                   {
                       Utilities.MostrarMensaje(MainActivity.this, "Error al iniciar sesión", "No se encontró una cuenta válida, por favor, comuníquese para más información");
                   }
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Se ha presentado un error, intenta de nuevo", Toast.LENGTH_SHORT).show();
                    OcultarDialogoCarga();
                }
            }
        });

    }

    private void GuardaDatosUsuario(DataSnapshot data) {
        String cNombreTaxista=data.child("cNombre").getValue()==null?"":data.child("cNombre").getValue().toString();
        String cTelefonoTaxista=data.child("cTelefono").getValue()==null?"":data.child("cTelefono").getValue().toString();
        String cIdMunicipio=data.child("cIdMunicipio").getValue()==null?"":data.child("cIdMunicipio").getValue().toString();
        Utilities.GuardarPreferencias("cNombreTaxista", cNombreTaxista, MainActivity.this);
        Utilities.GuardarPreferencias("cTelefonoTaxista", cTelefonoTaxista, MainActivity.this);
        Utilities.GuardarPreferencias("cIdMunicipio", cIdMunicipio, MainActivity.this);
        Utilities.GuardarPreferencias("cClaveUsuario", data.getKey(), this);
        Utilities.GuardarPreferencias("cCorreo",edCorreo.getText().toString(), this);
        Utilities.GuardarPreferencias("cCorreo",edCorreo.getText().toString(), this);
        Utilities.GuardarPreferencias("cUrlImagen", data.child("cUrlImagen").getValue()==null?"":data.child("cUrlImagen").getValue().toString(), MainActivity.this);

    }
}