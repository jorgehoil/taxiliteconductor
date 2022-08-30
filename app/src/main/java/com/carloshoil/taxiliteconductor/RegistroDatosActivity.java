package com.carloshoil.taxiliteconductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.carloshoil.taxiliteconductor.Dialog.DialogoCarga;
import com.carloshoil.taxiliteconductor.Global.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegistroDatosActivity extends AppCompatActivity {

    List<String> lstMunicipiosIDS;
    Spinner spPueblos;
    EditText edNombre, edTelefono;
    DatabaseReference databaseReference;
    DatabaseReference databaseReferenceUsers;
    FirebaseAuth firebaseAuth;
    Button btnGuardar;
    DialogoCarga dialogoCarga;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_datos);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Init();
    }

    private void Init() {
        lstMunicipiosIDS= new ArrayList<>();
        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        spPueblos= findViewById(R.id.spPueblosRegistro);
        edNombre=findViewById(R.id.edNombreRegistro);
        edTelefono=findViewById(R.id.edTelefonoRegistro);
        btnGuardar=findViewById(R.id.btnGuardarRegistro);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GuardaDatos();
            }
        });
        ObtenerMunicipiosActuales();

    }
    public void GuardaDatos()
    {
        HashMap<String, Object> obj= new HashMap<>();
        if(ValidaDatos())
        {
            if(firebaseAuth.getUid().isEmpty())
            {
                Utilities.MostrarMensaje(this, "Error", "No se ha iniciado sesión");
            }
            else
            {
                String cMunicipio=lstMunicipiosIDS.get(spPueblos.getSelectedItemPosition());
                if(cMunicipio.trim().isEmpty())
                {
                    Toast.makeText(this, "Error al seleccionar municipio", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    MostrarDialogoCarga();
                    obj.put("cNombre", edNombre.getText().toString());
                    obj.put("cTelefono", edTelefono.getText().toString());
                    obj.put("lBloqueado", true);
                    obj.put("iViajes", 0);
                    obj.put("cFechaRegistro",Utilities.obtenerFechaActualCadena());
                    obj.put("cFechaPago"," - - ");
                    obj.put("cIdMunicipio", lstMunicipiosIDS.get(spPueblos.getSelectedItemPosition()));
                    databaseReferenceUsers=databaseReference.child("taxistas").child(firebaseAuth.getUid());
                    databaseReferenceUsers.setValue(obj).addOnCompleteListener(task -> {
                        OcultarDialogoCarga();
                        if(task.isSuccessful())
                        {

                            Utilities.GuardarPreferencias("cEstatus", "COMP", RegistroDatosActivity.this);
                            GuardaDatosPreferencias();
                            Intent i= new Intent(RegistroDatosActivity.this, PrincipalActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else
                        {
                            Utilities.MostrarMensaje(RegistroDatosActivity.this, "Error al guardar", "Se ha producido un error al guardar, intenta de nuevo por favor");
                        }
                    });
                }
            }
        }

    }

    private void GuardaDatosPreferencias() {
        Utilities.GuardarPreferencias("cNombreTaxista", edNombre.getText().toString(), this);
        Utilities.GuardarPreferencias("cTelefonoTaxista", edTelefono.getText().toString(), this);
        Utilities.GuardarPreferencias("cIdMunicipio", lstMunicipiosIDS.get(spPueblos.getSelectedItemPosition()), this);
    }

    public boolean ValidaDatos()
    {
        if(edTelefono.getText().toString().isEmpty()&&edNombre.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Llene todos los campos por favor", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edTelefono.getText().toString().trim().length()!=10)
        {
            Toast.makeText(this, "Debe de ingresar 10 digitos para el teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void cargaSpinner(List<String> lstMunicipios)
    {
        spPueblos.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lstMunicipios));
        spPueblos.setSelection(0);
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
    public void ObtenerMunicipiosActuales()
    {
        spPueblos.setEnabled(false);
        databaseReference.child("municipios").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    List<String> listMunicipiosNombres= new ArrayList<>();
                    for(DataSnapshot dataSnapshot: task.getResult().getChildren())
                    {
                        String cNombreMunicipio=dataSnapshot.child("cNombre").getValue()==null?"":dataSnapshot.child("cNombre").getValue().toString();
                        String cIdMunicipio=dataSnapshot.getKey()==null?"":dataSnapshot.getKey();
                        listMunicipiosNombres.add(cNombreMunicipio);
                        lstMunicipiosIDS.add(cIdMunicipio);
                    }
                    cargaSpinner(listMunicipiosNombres);
                    spPueblos.setEnabled(true);
                }
                else
                {
                    ObtenerMunicipiosActuales();
                }


            }
        });
    }
}