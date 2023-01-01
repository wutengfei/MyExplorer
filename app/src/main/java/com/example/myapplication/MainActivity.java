package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url;
    private Button btn_enter;
    private WebView webview;
    private boolean mIsExit;
    private FrameLayout mLayout;
    private LinearLayout ll_title;
    private boolean isLandscape;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.color_notify_bar));

        et_url = findViewById(R.id.et_url);
        btn_enter = findViewById(R.id.btn_enter);
        webview = findViewById(R.id.webview);
        mLayout = findViewById(R.id.fl_video);
        ll_title = findViewById(R.id.ll_title);
        initWebView(webview);
        btn_enter.setOnClickListener(this);

        //在该Editview获得焦点的时候将“回车”键改为“搜索”
        et_url.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        et_url.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        //不然回车【搜索】会换行
        et_url.setSingleLine(true);
        et_url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) && keyEvent != null) {
                    //点击搜索要做的操作
                    search(et_url);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_enter) {
            search(view);
        }
    }

    private void search(View view) {
        String url = et_url.getText().toString().trim();
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("https") || url.startsWith("http")) {
                webview.loadUrl(url);
            } else {
                String url_enter = "https://" + url;
                boolean isMatchesUrl = Patterns.WEB_URL.matcher(url_enter).matches();
                if (isMatchesUrl) {
                    webview.loadUrl(url_enter);
                } else {
                    webview.loadUrl("https://www.baidu.com/s?wd=" + url);
                }

            }

        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initWebView(final WebView webView) {

        webView.getSettings().setAllowFileAccess(true);//设置在WebView内部是否允许访问文件，默认允许访问。
        webView.getSettings().setAllowFileAccessFromFileURLs(false);//设置WebView运行中的一个文件方案被允许访问其他文件方案中的内容，默认值true
        webView.getSettings().setAllowContentAccess(false);//设置WebView是否使用其内置的变焦机制，该机制结合屏幕缩放控件使用，默认是false，不使用内置变焦机制
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//设置WebView运行中的脚本可以是否访问任何原始起点内容，默认true
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(false);//设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setLoadsImagesAutomatically(true);//设置WebView是否加载图片资源，默认true，自动加载图片
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置脚本是否允许自动打开弹窗，默认false，不允许

        webView.getSettings().setSupportMultipleWindows(true);
        webView.setVerticalScrollBarEnabled(false);// 取消Vertical ScrollBar显示
        webView.setHorizontalScrollBarEnabled(false);// 取消Horizontal ScrollBar显示
        //设置自适应屏幕，两者合用
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setFocusable(false);// 去掉超链接的外边框
        webView.getSettings().setDefaultTextEncodingName("GBK");//设置文本编码（根据页面要求设置： utf-8）
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.setWebViewClient(new WebViewClient() {//拦截url让接口跳转只在本app内部跳转，不跳转浏览器
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                WebView.HitTestResult hitTestResult = view.getHitTestResult();
                //hitTestResult==null解决重定向问题
                if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                    view.loadUrl(url);
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }

            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains(".mp4") || url.contains(".m3u8?") || url.contains(".avi") ||
                        url.contains(".mov") || url.contains(".mkv") || url.contains(".flv") ||
                        url.contains(".f4v") || url.contains(".rmvb")) {

                    Log.e("video_url", url);

                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {//防止遇到重定向
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

        webView.setDownloadListener(new DownloadListener() {//下载监听，跳转到系统浏览器去下载
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(uri);
                startActivity(intent);

            }
        });

    }


    /**
     * 横竖屏切换监听
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;

            case Configuration.ORIENTATION_PORTRAIT:

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        private CustomViewCallback mCustomViewCallback;
        //  横屏时，显示视频的view
        private View mCustomView;

        // 全屏的时候调用
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            isLandscape = true;
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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            webview.setVisibility(View.GONE);
        }

        // 切换为竖屏的时候调用
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            isLandscape = false;
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
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isLandscape) {//当横屏时isLandscape为true，返回到竖屏时先调用onKeyDown()，再调用onHideCustomView()，所以此时isLandscape仍是true
                return false;
            }

            if (webview.canGoBack()) {
                webview.goBack();
                return true;
            }

            if (mIsExit) {
                this.finish();
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mIsExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsExit = false;
                    }
                }, 2000);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webview != null) {
            ViewParent parent = webview.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(webview);
            }
            webview.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            webview.getSettings().setJavaScriptEnabled(false);
            webview.clearHistory();
            webview.clearView();
            webview.removeAllViews();
            webview.destroy();
            webview = null;
        }
        super.onDestroy();
    }
}



