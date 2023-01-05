package com.feige.myexplorer.utils;

import android.content.Context;
import android.view.View;

import com.feige.myexplorer.dialog.MyDialog;

/**
 * Author: wutengfei
 * Date: 2023/1/5
 * Description:
 */
public class OtherUtils {
    public static void showAlert(Context context, String title, String content, View.OnClickListener listener, boolean hasCancel) {
        new MyDialog(context, title, content, listener, hasCancel).show();
    }

    public static void showAlert(Context context, String content) {
        new MyDialog(context, "", content, null, false).show();
    }
}
