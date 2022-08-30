package com.carloshoil.taxiliteconductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.carloshoil.taxiliteconductor.Global.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

public class PrincipalActivity extends AppCompatActivity {

    ToggleButton btnConectar;
    DatabaseReference databaseReferenceAvailable;
    DatabaseReference databaseReferenceMain;
    DatabaseReference databaseReferenceUsers;
    Button btnData;
    Button btnCerrarSesion;
    TextView tvNombreUsuario;
    FirebaseAuth firebaseAuth;
    boolean lConectado=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Init();
        AjustaBotonConectar();
        AjustaReferencia();
    }

    private void Init() {
        Log.d("DEBUG", "---- Init ----");
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReferenceMain=FirebaseDatabase.getInstance().getReference();
        databaseReferenceUsers=databaseReferenceMain.child("taxistas").child(firebaseAuth.getUid());
        btnCerrarSesion=findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lConectado)
                {
                    ConfirmaCerrarSesion();
                }else
                {
                    Toast.makeText(PrincipalActivity.this, "Desconéctate para poder cerrar la sesión.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnConectar=findViewById(R.id.btnConectar);
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Conectar(!lConectado);
            }
        });
        btnData=findViewById(R.id.btnData);
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lConectado)
                {
                    Toast.makeText(PrincipalActivity.this, "Por favor, desconéctate para modificar tus datos.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent i= new Intent(PrincipalActivity.this, ActivityDatos.class);
                    startActivity(i);
                }


            }
        });
        tvNombreUsuario=findViewById(R.id.edNombreBienvenido);
        tvNombreUsuario.setText(Utilities.RecuperaPreferencia("cNombreTaxista", this).isEmpty()?"":Utilities.RecuperaPreferencia("cNombreTaxista", this));
    }

    private void AjustaBotonConectar()
    {
        Log.d("DEBUG", "---- AjustaBotonConectar  ----");
        lConectado=Utilities.RecuperaPreferencia("lConectado", this).equals("1");
        if(lConectado)
        {
            btnConectar.setChecked(true);
            btnConectar.setText("CONECTADO");
        }
        else
        {
            btnConectar.setChecked(false);
            btnConectar.setText("DESCONECTADO");
        }
    }

    private void AjustaReferencia() {
        Log.d("DEBUG", "---- AjustaReferencia  ----");
        String cTaxiLink="available"+ Utilities.RecuperaPreferencia("cIdMunicipio",this);
        databaseReferenceAvailable=databaseReferenceMain.child("taxis").child(cTaxiLink).child(firebaseAuth.getUid());
    }
    private void Conectar(boolean lConectar)
    {
        Log.d("DEBUG", "---- Conectar  ----");
        if(lConectar)
        {
            btnConectar.setEnabled(false);
            btnConectar.setText("Conectando...");
            VerificaBloqueo();
        }
        else
        {
            btnConectar.setEnabled(false);
            btnConectar.setText("Desconectando...");
            databaseReferenceAvailable.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    btnConectar.setEnabled(true);
                    if(task.isSuccessful())
                    {
                        btnConectar.setText("DESCONECTADO");
                        btnConectar.setChecked(false);
                        Utilities.GuardarPreferencias("lConectado", "0", PrincipalActivity.this);
                        lConectado=false;
                    }
                    else
                    {
                        btnConectar.setText("CONECTADO");
                        btnConectar.setChecked(true);
                        Utilities.MostrarMensaje(PrincipalActivity.this, "Error al desconectar", "Se ha presentado un error al desconetar, intenta de nuevo por favor");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    btnConectar.setText("CONECTADO");
                    btnConectar.setChecked(true);
                    Utilities.MostrarMensaje(PrincipalActivity.this, "Error al desconectar", "Se ha presentado un error al desconetar, intenta de nuevo por favor");
                }
            });
        }

    }

    private void VerificaBloqueo() {
        Log.d("DEBUG", "---- VerificaBloqueo  ----");
        databaseReferenceUsers.child("lBloqueado").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                boolean lBloqueado=false;
                if(task.isSuccessful())
                {
                    lBloqueado=task.getResult().getValue(Boolean.class);
                     if(lBloqueado)
                     {
                         Utilities.MostrarMensaje(PrincipalActivity.this, "Cuenta bloqueada", "Lo sentimos, la cuenta está bloqueada, comunícate para desbloquearla");
                         btnConectar.setEnabled(true);
                         btnConectar.setText("DESCONECTADO");
                         btnConectar.setChecked(false);
                     }
                     else
                     {
                         VerificaTiempoViajes();
                     }
                }
                else
                {
                    Utilities.MostrarMensaje(PrincipalActivity.this, "Error", "Se ha presentado un error al intentar conectar, verifica tu conexión a internet");
                    btnConectar.setEnabled(true);
                    btnConectar.setText("DESCONECTADO");
                    btnConectar.setChecked(false);
                }
            }
        });

    }

    private void CreaRegistroConexion(int iViajes)
    {
        Log.d("DEBUG", "---- CreaRegistroConexion  ----");
        HashMap<String, Object> obj= new HashMap<>();
        obj.put("cNombre", Utilities.RecuperaPreferencia("cNombreTaxista", this));
        obj.put("cTelefono", Utilities.RecuperaPreferencia("cTelefonoTaxista", this));
        obj.put("cUrlImagen", Utilities.RecuperaPreferencia("cUrlImagen",this));
        obj.put("iViajes", iViajes);
        databaseReferenceAvailable.setValue(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                btnConectar.setEnabled(true);
                if(task.isSuccessful())
                {
                    btnConectar.setChecked(true);
                    btnConectar.setText("CONECTADO");
                    Utilities.GuardarPreferencias("lConectado", "1", PrincipalActivity.this);
                    lConectado=true;
                }
                else
                {
                    Utilities.MostrarMensaje(PrincipalActivity.this,"Error al conectar", "Se ha presentado un error al intentar conectar, verifica tu conexión a internet");
                    btnConectar.setChecked(false);
                    btnConectar.setText("DESCONECTADO");
                }
            }
        });
    }
    private void ObtenerViajes() {
        Log.d("DEBUG", "---- ObtenerViajes  ----");
        databaseReferenceUsers.child("iViajes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    int iViajes= task.getResult().getValue()==null?0:task.getResult().getValue(Integer.class);
                    CreaRegistroConexion(iViajes);
                }
                else
                {
                    Utilities.MostrarMensaje(PrincipalActivity.this, "Error", "Se ha presentado un error al conectar, por favor verifica tu conexión a internet");
                    btnConectar.setEnabled(true);
                    btnConectar.setText("DESCONECTADO");
                    btnConectar.setChecked(false);
                }
            }
        });

    }

    private void ConfirmaCerrarSesion()
    {
        Log.d("DEBUG", "---- ConfirmaCerrarSesion  ----");
        AlertDialog.Builder dialog= new AlertDialog.Builder(this);
        dialog.setTitle("¿Seguro que deseas cerrar la sesión?");
        dialog.setMessage("Si solo quieres tomarte un descanso, puedes desconectarte y salir de la app sin cerrar sesión");
        dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CerrarSesion();
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void CerrarSesion()
    {
        Log.d("DEBUG", "---- Cerrar Sesion  ----");
        FirebaseAuth.getInstance().signOut();
        Utilities.GuardarPreferencias("cEstatus", "", this);
        Intent i= new Intent(PrincipalActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void VerificaTiempoViajes()
    {

        Log.d("DEBUG", "-------VerificaTiempoBloqueo()-------");
        Random random= new Random();
        String cFechaInicio=Utilities.RecuperaPreferencia("cFechaInicio",PrincipalActivity.this);
        if(!cFechaInicio.trim().isEmpty())
        {
            long lTiempo;
            boolean lRespuesta=false;
            String cFechaActual=Utilities.obtenerFechaActualCadena();
            lTiempo=Utilities.obtenerSegundos(cFechaInicio, cFechaActual);
            Log.d("DEBUG","lTiempo:" + lTiempo);
            if(lTiempo>86400) {
                Utilities.GuardarPreferencias("cFechaInicio",Utilities.obtenerFechaActualCadena(),PrincipalActivity.this);
                ReiniciaConteoViajes();
            }
            else
            {
                ObtenerViajes();
            }
        }
        else
        {
            Utilities.GuardarPreferencias("cFechaInicio",Utilities.obtenerFechaActualCadena(),PrincipalActivity.this);
            ObtenerViajes();
        }

    }

    private void ReiniciaConteoViajes()
    {
        Log.d("DEBUG", "---- Se Reinicia Conteo Viajes  ----");
        databaseReferenceUsers.child("iViajes").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    CreaRegistroConexion(0);
                }
                else
                {
                    Utilities.MostrarMensaje(PrincipalActivity.this, "Error", "Se ha presentado un error al conectar, intente de nuevo");
                    btnConectar.setEnabled(true);
                    btnConectar.setText("DESCONECTADO");
                    btnConectar.setChecked(false);
                }
            }
        });

    }


}