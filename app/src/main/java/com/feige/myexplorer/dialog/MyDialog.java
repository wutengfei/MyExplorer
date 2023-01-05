package com.feige.myexplorer.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.feige.myexplorer.R;

public class MyDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private String title, content;
    private View.OnClickListener onClickListener;
    private boolean hasCancelbtn = true;

    public MyDialog(@NonNull Context context, String title, String content, View.OnClickListener onClickListener, boolean hasCancelbtn) {
        super(context, R.style.MyDialog);
        mContext = context;
        this.title = title;
        this.content = content;
        this.onClickListener = onClickListener;
        this.hasCancelbtn = hasCancelbtn;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog, null);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvContent = view.findViewById(R.id.tv_content);
        View line = view.findViewById(R.id.line);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnOK = view.findViewById(R.id.btn_ok);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        setContentView(view);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        attributes.gravity = Gravity.CENTER;
        attributes.width = (int) (display.getWidth() * 0.8);
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(attributes);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        if (!hasCancelbtn) {
            line.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        } else {
            line.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                dismiss();
                if (onClickListener != null) {
                    onClickListener.onClick(view);
                }
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }

    }

}
