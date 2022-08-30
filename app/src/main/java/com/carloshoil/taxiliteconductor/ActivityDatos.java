 package com.carloshoil.taxiliteconductor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompatSideChannelService;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.carloshoil.taxiliteconductor.Dialog.DialogoCarga;
import com.carloshoil.taxiliteconductor.Global.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class ActivityDatos extends AppCompatActivity {

    Switch swCambiaPass;
    Button btnGuardaDatos, btnRotar;
    TextView tvContrasenaAnt, tvContrasenaNue, tvConfirmacionCont;
    EditText edNombre, edTelefono,edContrasenaAnterior, edContrasena, edConfirmaContrasena, edCorreo;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    DialogoCarga dialogoCarga;
    FirebaseStorage storage;
    StorageReference storageRef;
    StorageReference storageReferenceImagenPerfil;
    ImageView imPerfil;
    ActivityResultLauncher<String> mGetContent;
    boolean lImagenCargada=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Init();
        CargaDatos();

    }

    private void CargaDatos() {
        String cNombre=Utilities.RecuperaPreferencia("cNombreTaxista",ActivityDatos.this);
        String cTelefono=Utilities.RecuperaPreferencia("cTelefonoTaxista",ActivityDatos.this);
        String cCorreo=Utilities.RecuperaPreferencia("cCorreo", ActivityDatos.this);
        String cUrlImagen=Utilities.RecuperaPreferencia("cUrlImagen", ActivityDatos.this);
        edNombre.setText(cNombre);
        edTelefono.setText(cTelefono);
        edCorreo.setText(cCorreo);
        if(!cUrlImagen.isEmpty())
        {
            Glide.with(ActivityDatos.this)
                    .load(cUrlImagen)
                    .into(imPerfil);
        }
        else
        {
            imPerfil.setImageDrawable(ActivityDatos.this.getResources().getDrawable(R.drawable.imgavatar));
        }
    }


    private void Init()
    {

        storage = FirebaseStorage.getInstance();
        storageRef=storage.getReference();
        databaseReference= FirebaseDatabase.getInstance().getReference();
        swCambiaPass=findViewById(R.id.swCambiaPass);
        edNombre = findViewById(R.id.edNombreTaxista);
        edTelefono = findViewById(R.id.edTelefonoTaxista);
        edConfirmaContrasena = findViewById(R.id.edConfirmaContrasena);
        edContrasena = findViewById(R.id.edContrasena);
        edContrasenaAnterior=findViewById(R.id.edContrasenaAnterior);
        edCorreo=findViewById(R.id.edCorreoTaxista);
        imPerfil=findViewById(R.id.imPerfil);
        tvConfirmacionCont= findViewById(R.id.tvConfirmaContrasena);
        tvContrasenaAnt=findViewById(R.id.tvContrasenaAnt);
        tvContrasenaNue=findViewById(R.id.tvContrasenaNueva);
        btnGuardaDatos=findViewById(R.id.btnGuardaDatosTaxi);
        btnRotar=findViewById(R.id.btnRotar);
        btnRotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rotar();
            }
        });
        swCambiaPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MostrarOcultar(isChecked);
            }
        });
        btnGuardaDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidaGuardaDatos();
            }
        });
        imPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria();
            }
        });
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        storageReferenceImagenPerfil=storageRef.child("image_profile/"+firebaseAuth.getUid()+".jpg");
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    InputStream inputStream;
                    if(uri!=null)
                    {
                        String cPath=uri.getPath();
                        if(cPath!=null &&!cPath.isEmpty())
                        {
                            inputStream=null;
                            try {
                                inputStream=getContentResolver().openInputStream(uri);

                            }catch(Exception ex)
                            {
                                Toast.makeText(this, "Error al carga imagen", Toast.LENGTH_SHORT).show();
                            }
                            Bitmap imageBitmap= BitmapFactory.decodeStream(inputStream);
                            Bitmap bmFinal;
                            Log.d("DEBUG", "Width: "+ imageBitmap.getWidth()+ "Height: "+imageBitmap.getHeight());
                            if(imageBitmap.getWidth()>=imageBitmap.getHeight())
                            {
                                Log.d("DEBUG", "Width es mayor o igual que height");
                                bmFinal=Bitmap.createBitmap(imageBitmap,
                                        imageBitmap.getWidth()/2-imageBitmap.getHeight()/2,
                                        0,
                                        imageBitmap.getHeight(),
                                        imageBitmap.getHeight());
                            }
                            else
                            {
                                bmFinal=Bitmap.createBitmap(imageBitmap,
                                        0,
                                        imageBitmap.getHeight()/2-imageBitmap.getWidth()/2,
                                        imageBitmap.getWidth(),
                                        imageBitmap.getWidth());
                            }

                            bmFinal=Bitmap.createScaledBitmap(bmFinal, 125, 125, false);
                            imPerfil.setImageBitmap(bmFinal);
                            lImagenCargada=true;
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show();
                        lImagenCargada=false;
                    }

                });
    }

    private void ValidaGuardaDatos() {
        if(ValidaCamposGenerales())
        {
            if(swCambiaPass.isChecked())
            {
                if(ValidaCamposPass())
                {
                    MostrarDialogoCarga();
                   CambiaContrasena(edContrasenaAnterior.getText().toString(),edContrasena.getText().toString());
                }
            }
            else
            {
                MostrarDialogoCarga();
                GuardaDatos();
            }
        }

    }


    private void GuardaDatos() {
        HashMap<String, Object> data= new HashMap<>();
        data.put("cTelefono", edTelefono.getText().toString());
        databaseReference.child("taxistas").child(user.getUid()).updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    if(!lImagenCargada)
                    {
                        Toast.makeText(ActivityDatos.this, "¡Se ha guardado con éxito los datos!", Toast.LENGTH_SHORT).show();
                        Utilities.GuardarPreferencias("cTelefonoTaxista", edTelefono.getText().toString(), ActivityDatos.this);
                        OcultarDialogoCarga();
                        LimpiaCampos();
                    }else
                    {

                        subirImagen();
                    }
                }
                else
                {
                    OcultarDialogoCarga();
                    Utilities.MostrarMensaje(ActivityDatos.this, "Error al guardar", "Se ha producido un error al guardar");

                }
            }
        });
    }

    public void CambiaContrasena(String cContrasenaActual, String cContrasenaNueva)
    {
        String cCorreo=Utilities.RecuperaPreferencia("cCorreo", this);
        AuthCredential credential = EmailAuthProvider
                .getCredential(cCorreo,cContrasenaActual);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    user.updatePassword(cContrasenaNueva).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            GuardaDatos();
                        }
                    });
                }
                else
                {
                    OcultarDialogoCarga();
                    Utilities.MostrarMensaje(ActivityDatos.this, "Error de constraseña", "La contraseña actual es incorrecta, verifica por favor");
                }
            }
        });
    }

    private boolean ValidaCamposGenerales()
    {
        if(edTelefono.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Ingrese el número de teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edTelefono.getText().toString().trim().length()!=10)
        {
            Toast.makeText(this, "Ingrese 10 dígitos del teléfono", Toast.LENGTH_SHORT).show();
            return false;
        }
     return true;

    }
    private void abrirGaleria()
    {
        mGetContent.launch("image/*");
    }
    private boolean ValidaCamposPass(){
        if(edContrasena.getText().toString().trim().isEmpty()||edConfirmaContrasena.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this, "Llene todos los datos para continuar", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!edContrasena.getText().toString().equals(edConfirmaContrasena.getText().toString()))
        {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void MostrarOcultar(boolean lMostrar)
    {
        if(lMostrar)
        {
            tvContrasenaNue.setVisibility(View.VISIBLE);
            tvContrasenaAnt.setVisibility(View.VISIBLE);
            tvConfirmacionCont.setVisibility(View.VISIBLE);
            edContrasenaAnterior.setVisibility(View.VISIBLE);
            edContrasena.setVisibility(View.VISIBLE);
            edConfirmaContrasena.setVisibility(View.VISIBLE);
        }
        else
        {
            tvContrasenaNue.setVisibility(View.GONE);
            tvContrasenaAnt.setVisibility(View.GONE);
            tvConfirmacionCont.setVisibility(View.GONE);
            edContrasenaAnterior.setVisibility(View.GONE);
            edContrasena.setVisibility(View.GONE);
            edConfirmaContrasena.setVisibility(View.GONE);
        }
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
    private void Rotar()
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = ((BitmapDrawable) imPerfil.getDrawable()).getBitmap();
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imPerfil.setImageBitmap(rotatedBitmap);
        lImagenCargada=true;

    }
    public void subirImagen()
    {
        imPerfil.setDrawingCacheEnabled(true);
        imPerfil.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imPerfil.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageReferenceImagenPerfil.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Utilities.MostrarMensaje(ActivityDatos.this, "Error al guardar", "Se ha producido un error al intentar subir imagen");
            OcultarDialogoCarga();
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Utilities.GuardarPreferencias("cUrlImagen", uri.toString(), ActivityDatos.this);
                        databaseReference.child("taxistas").child(user.getUid()).child("cUrlImagen").setValue(uri.toString());
                    }
                });
                Toast.makeText(ActivityDatos.this, "¡Se ha guardado con éxito los datos!", Toast.LENGTH_SHORT).show();
                Utilities.GuardarPreferencias("cTelefonoTaxista", edTelefono.getText().toString(), ActivityDatos.this);
                OcultarDialogoCarga();
                LimpiaCampos();
                lImagenCargada=false;
            }
        });
    }
    private void LimpiaCampos()
    {
        edConfirmaContrasena.setText("");
        edContrasena.setText("");
        edContrasenaAnterior.setText("");
    }
}