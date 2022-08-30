package com.carloshoil.taxiliteconductor.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.carloshoil.taxiliteconductor.R;

public class DialogoCarga extends DialogFragment {

    Context context;

    public DialogoCarga(Context context)
    {
        this.context=context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return creaDialogo();
    }

    private Dialog creaDialogo()
    {
        AlertDialog.Builder alert= new AlertDialog.Builder(context);
        LayoutInflater layoutInflater= getActivity().getLayoutInflater();
        View view =layoutInflater.inflate(R.layout.dialogo_carga,null);
        alert.setView(view);
        return alert.create();
    }
}
