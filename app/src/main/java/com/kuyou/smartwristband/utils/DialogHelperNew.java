package com.kuyou.smartwristband.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;


import com.kuyou.smartwristband.R;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * 自定义Dialog。
 */

public class DialogHelperNew {
    private static Dialog waitDialog;

    /**
     * 创建等待Dialog
     *
     * @param c
     *         上下文环境
     * @param cancelable
     *         Dialog是否可以返回取消
     *
     * @return Dialog
     */
    public static Dialog buildWaitDialog(Context c, boolean cancelable) {
        if(waitDialog == null){
            waitDialog = new Dialog(c, R.style.theme_dialog);
        }
        waitDialog.setContentView(LayoutInflater.from(c).inflate(R.layout.dialog_wait,null));
        waitDialog.setCancelable(cancelable);
        waitDialog.show();
        return waitDialog;
    }

    public static void dismissWait(){
        if (waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
    }


    public static Dialog showRemindDialog(Activity context, String title, String tips,
                                          String sureText,View.OnClickListener listener,
                                          View.OnClickListener canleListener){
        Dialog dialog = new Dialog(context, R.style.center_dialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_remind,null);
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        dialogTitle.setText(title);
        TextView tvTips = view.findViewById(R.id.tvTips);
        tvTips.setText(tips);
        view.findViewById(R.id.tvCanle).setOnClickListener(v -> {
            dialog.dismiss();
            canleListener.onClick(view);
        });
        TextView tvSure = view.findViewById(R.id.tvSure);
        tvSure.setText(sureText);
        tvSure.setOnClickListener(v ->{
            dialog.dismiss();
            listener.onClick(view);
        });
        dialog.setContentView(view);
        dialog.setCancelable(false);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
        dialog.show();
        return dialog;
    }

}
