package com.carloshoil.taxiliteconductor.Global;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.carloshoil.taxiliteconductor.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utilities {
    public static void GuardarPreferencias(String cClave, String cValor, Context context)
    {
        SharedPreferences sharedPreferences= context.getSharedPreferences(context.getString(R.string.name_filepreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(cClave, cValor);
        editor.commit();
    }
    public static String RecuperaPreferencia(String cClave, Context context)
    {
        String cRespuesta;
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getString(R.string.name_filepreferences), Context.MODE_PRIVATE);
        cRespuesta=sharedPreferences.getString(cClave, "");
        return cRespuesta;
    }
    public static void MostrarMensaje(Context context, String cTitulo, String cMensaje)
    {
        AlertDialog.Builder alert= new AlertDialog.Builder(context);
        alert.setTitle(cTitulo);
        alert.setMessage(cMensaje);
        alert.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        alert.show();
    }
    public static Date obtenerFechaActual()
    {
        return Calendar.getInstance().getTime();
    }

    public static String obtenerFechaActualCadena()
    {
        String cRetorno="";
        Date dtFechaActual;
        dtFechaActual=obtenerFechaActual();
        cRetorno=formatDate(dtFechaActual);
        return cRetorno;
    }
    public static String formatDate(Date _date)
    {
        String cResultado;
        try{
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            cResultado=simpleDateFormat.format(_date);
            return cResultado;

        }catch (Exception ex)
        {
            return "00-0000-0000 00:00:00";
        }
    }
    public static long obtenerSegundos(String cDateAntigua, String cDateActual)
    {
        Log.d("DEBUG", "FECHA ACTUAL:"+cDateActual+" FECHA ANTIGUA:"+cDateAntigua);
        long lSegundosRetorno=0, lDifference=0;
        Date dtFechaInicio;
        Date dtFechaFinal;
        try{
            SimpleDateFormat cDateFormat=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
            dtFechaInicio= cDateFormat.parse(cDateAntigua);
            dtFechaFinal=cDateFormat.parse(cDateActual);
            lDifference=dtFechaFinal.getTime()-dtFechaInicio.getTime();
            if(lDifference>0)
            {
                lSegundosRetorno=lDifference/1000;
            }
            return lSegundosRetorno;
        }
        catch (Exception e)
        {
            Log.e("DEBUG", e.getMessage());
            return 0;
        }

    }
}
