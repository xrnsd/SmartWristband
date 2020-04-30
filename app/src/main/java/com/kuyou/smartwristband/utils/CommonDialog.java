package com.kuyou.smartwristband.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kuyou.smartwristband.utils.ScreenUtil;
import com.kuyou.smartwristband.R;


/**
 */
public class CommonDialog extends Dialog {
    public CommonDialog(Context context) {
        super(context);
    }

    public CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private String mMessage;
    public String getMessage(){
        return mMessage;
    }

    private Activity mContext;
    public Activity getActivityContext(){
        return mContext;
    }
    public void setContext(Activity context){
        mContext =context;
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String negativeButtonText;
        private String positiveButtonText;
        private boolean cancelable;
        private View contentView;
        private OnClickListener negativeButtonOnClickListener;
        private OnClickListener positiveButtonOnClickListener;
        private boolean isVertical = false;
        private int leftTextColor = -1;
        private int rightTextColor = -1;
        private int titleTextColor = -1;
        private int messageTextColor = -1;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(int title) {
            this.title = context.getString(title);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = context.getString(message);
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setLeftTextColor(int colorRes){
            this.leftTextColor = context.getResources().getColor(colorRes);
            return this;
        }

        public Builder setRightTextColor(int colorRes){
            this.rightTextColor = context.getResources().getColor(colorRes);
            return this;
        }

        public Builder setTitleTextColor(int colorRes){
            this.titleTextColor = context.getResources().getColor(colorRes);
            return this;
        }

        public Builder setMessageTextColor(int colorRes){
            this.messageTextColor = context.getResources().getColor(colorRes);
            return this;
        }

        public Builder isVertical(boolean isVertical) {
            this.isVertical = isVertical;
            return this;
        }
        /**
         * true:按返回键可dismiss
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * 确定
         *
         * @param positiveButtonText
         * @param positiveOnClickListener
         * @return
         */
        public Builder setRightButton(String positiveButtonText, OnClickListener positiveOnClickListener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonOnClickListener = positiveOnClickListener;
            return this;
        }

        public Builder setRightButton(int positiveButtonText, OnClickListener positiveOnClickListener) {
            this.positiveButtonText = context.getString(positiveButtonText);
            this.positiveButtonOnClickListener = positiveOnClickListener;
            return this;
        }
        public Builder setRightButton(OnClickListener positiveOnClickListener) {
            this.positiveButtonOnClickListener = positiveOnClickListener;
            return this;
        }

        public Builder setLeftButton(String negativeButtonText, OnClickListener negativeOnClickListener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonOnClickListener = negativeOnClickListener;
            return this;
        }

        /**
         * set the cancel button  
         *
         * @param negativeButtonText
         * @param negativeOnClickListener
         * @return
         */
        public Builder setLeftButton(int negativeButtonText, OnClickListener negativeOnClickListener) {
            this.negativeButtonText = context.getString(negativeButtonText);
            this.negativeButtonOnClickListener = negativeOnClickListener;
            return this;
        }
        public Builder setLeftButton(int negativeButtonText) {
            this.negativeButtonText = context.getString(negativeButtonText);
            return this;
        }

        public Builder setView(View view) {
            this.contentView = view;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        private  int type=-1;

        public CommonDialog create() {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            final CommonDialog dialog = new CommonDialog(context, R.style.dialog);
            View layout = null;
            if (type==-1){
                layout = layoutInflater.inflate(isVertical ? R.layout.common_dialog_vertical_layout : R.layout.common_dialog_layout, null);
            }else{
                layout = layoutInflater.inflate(R.layout.common_dialog_vertical_layout2 , null);
            }

            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialog.setCancelable(cancelable); //true:按返回键可dismiss
            dialog.setCanceledOnTouchOutside(true);
            if (type==-1) {
                if (!isVertical) {
                    dialog.getWindow().getAttributes().width = (int) (ScreenUtil.getScreenWidth(context) * 0.75f);
                }
            }else{
                dialog.getWindow().getAttributes().width = (int) (ScreenUtil.getScreenWidth(context) * 0.9f);
                dialog.getWindow().setGravity(Gravity.BOTTOM);
            }
            if (!TextUtils.isEmpty(title)) {
                ((TextView) layout.findViewById(R.id.title)).setText(title);
                if(titleTextColor!=-1) {
                    ((TextView) layout.findViewById(R.id.title)).setTextColor(titleTextColor);
                }
            } else {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(message)) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
                if(messageTextColor!=-1) {
                    ((TextView) layout.findViewById(R.id.message)).setTextColor(messageTextColor);
                }
                dialog.mMessage=message;
            } else if (contentView != null) {
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
            if (!TextUtils.isEmpty(positiveButtonText)) {
                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);

                (layout.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (positiveButtonOnClickListener != null) {
                            positiveButtonOnClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    }
                });
                if(rightTextColor!=-1){
                    ((Button) layout.findViewById(R.id.positiveButton)).setTextColor(rightTextColor);
                }

            } else {
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
                layout.findViewById(R.id.bottom_line).setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(negativeButtonText)) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);

                (layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (negativeButtonOnClickListener != null) {
                            negativeButtonOnClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    }
                });
                if(leftTextColor!=-1){
                    ((Button) layout.findViewById(R.id.negativeButton)).setTextColor(leftTextColor);
                }

            } else {
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
                layout.findViewById(R.id.bottom_line).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }
    }

}
