package com.feige.myexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;

/**
 * Author: wutengfei
 * Date: 2023/1/6
 * Description: 全屏播放视频设置
 */

public class MyWebChromeClient extends WebChromeClient {
    private Context context;
    private FrameLayout mLayout;
    private WebView webview;
    private ProgressBar progress;
    private LinearLayout ll_title;
    private CustomViewCallback mCustomViewCallback;
    //  横屏时，显示视频的view
    private View mCustomView;

    public MyWebChromeClient(Context context, FrameLayout frameLayout, WebView webView, LinearLayout ll_title, ProgressBar progress) {
        this.context = context;
        mLayout = frameLayout;
        webview = webView;
        this.ll_title = ll_title;
        this.progress = progress;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        progress.setProgress(newProgress);
        if (newProgress == 100) {
            progress.setVisibility(View.INVISIBLE);//加载完网页进度条消失
        } else {
            progress.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
            progress.setProgress(newProgress);//设置进度值
        }
        super.onProgressChanged(view, newProgress);
    }

    // 全屏的时候调用
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        MainActivity.isLandscape = true;
        //如果view 已经存在，则隐藏
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mCustomView = view;
        mCustomView.setVisibility(View.VISIBLE);
        mCustomViewCallback = callback;
        mLayout.addView(mCustomView);
        mLayout.setVisibility(View.VISIBLE);
        mLayout.bringToFront();
        ll_title.setVisibility(View.GONE);
        //设置横屏
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        webview.setVisibility(View.GONE);
    }

    // 切换为竖屏的时候调用
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onHideCustomView() {
        super.onHideCustomView();
        MainActivity.isLandscape = false;
        ll_title.setVisibility(View.VISIBLE);
        webview.setVisibility(View.VISIBLE);
        if (mCustomView == null) {
            mLayout.setVisibility(View.GONE);
            return;
        }
        mCustomView.setVisibility(View.GONE);
        mLayout.removeView(mCustomView);
        mCustomView = null;
        mLayout.setVisibility(View.GONE);
        try {
            mCustomViewCallback.onCustomViewHidden();
        } catch (Exception e) {
        }
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
    }

}



