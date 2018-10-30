
package com.gk.sam;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class CustomProgressDialog {
    private static Dialog mDialog = null;

    public CustomProgressDialog() {
        super();
    }

    public static void showProgressDialog(Context mContext, String text, boolean cancellable) {
        removeDialog();
        mDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View layout = mInflater.inflate(R.layout.progress_dialog, null);
        mDialog.setContentView(layout);
        TextView tvProgressDescription = (TextView) layout.findViewById(R.id.progressDescription);
        if (text.equals("")) {
            tvProgressDescription.setVisibility(View.GONE);
        } else {
            tvProgressDescription.setText(text);
        }
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        return true;
                    case KeyEvent.KEYCODE_SEARCH:
                        return true;
                }

                return false;
            }
        });
        mDialog.setCancelable(cancellable);

        if (mDialog != null) {
            try {
                mDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static synchronized void removeDialog() {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDialog = null;
        }

    }
}
