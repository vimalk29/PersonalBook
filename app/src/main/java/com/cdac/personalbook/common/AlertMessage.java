package com.cdac.personalbook.common;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public class AlertMessage {
    public static void showMessageDialog(Activity activity, String msg) {
        final AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
        ;
        AlertDialog alert = builder.create();
        if(!activity.isFinishing())
            alert.show();
    }

    public static void showMessageDialog(Activity activity, String msg, final OkListener listener) {
        final AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onOkClicked();
                    }
                })
        ;
        AlertDialog alert = builder.create();
        if(!activity.isFinishing())
            alert.show();
    }

    public static void showMessageDialog(Activity activity, String msg, String yes, String no, final YesNoListener listener) {
        final AlertDialog.Builder builder= new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDecision(true);
                    }
                })
                .setNegativeButton(no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDecision(false);
                    }
                })
        ;
        AlertDialog alert = builder.create();
        if(!activity.isFinishing())
            alert.show();
    }


    public interface OkListener {
        void onOkClicked();
    }

    public interface YesNoListener {
        void onDecision(boolean btnClicked);
    }
}