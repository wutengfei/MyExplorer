package com.feige.myexplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import cn.org.bjca.signet.component.qr.activity.SignetQrApi;
import cn.org.bjca.signet.component.qr.bean.QrResultBean;
import cn.org.bjca.signet.component.qr.callback.QrBaseCallBack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url;
    private Button btn_enter;
    private WebView webview;
    private ImageView qr_scan;
    private boolean mIsExit;
    private FrameLayout mLayout;
    private LinearLayout ll_title;
    private boolean isLandscape;
    private Context context;
    private static final int QRSCAN_REQ_CAMERA_PERMISSION = 776;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.color_notify_bar));

        et_url = findViewById(R.id.et_url);
        btn_enter = findViewById(R.id.btn_enter);
        qr_scan = findViewById(R.id.qr_scan);
        webview = findViewById(R.id.webview);
        mLayout = findViewById(R.id.fl_video);
        ll_title = findViewById(R.id.ll_title);
        btn_enter.setOnClickListener(this);
        qr_scan.setOnClickListener(this);
        initWebView(webview);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_enter) {
            search(view);
        } else if (view.getId() == R.id.qr_scan) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)) {
                ((Activity) context).requestPermissions(new String[]{Manifest.permission.CAMERA}, QRSCAN_REQ_CAMERA_PERMISSION);
            } else {
                qrScan();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void search(View view) {
        webview.removeAllViews();
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

    private void qrScan() {
        SignetQrApi.useQrFunc(new QrBaseCallBack(context) {
            @Override
            public void onQrResult(final QrResultBean result) {
                if (result.getQrCode() == null) {
                    Toast.makeText(context, "扫描结果为空", Toast.LENGTH_SHORT).show();
                } else {
                    String str = result.getQrCode();
                    if (webview != null) {
                        boolean isMatchesUrl = Patterns.WEB_URL.matcher(str).matches();
                        if (isMatchesUrl) {
                            webview.loadUrl(str);
                        } else {
                            TextView textView = new TextView(context);
                            textView.setPadding(50, 30, 50, 30);
                            textView.setTextIsSelectable(true);
                            textView.setText(str);
                            textView.setTextColor(Color.BLACK);

                            webview.removeAllViews();
                            webview.clearHistory();
                            webview.clearView();
                            webview.loadUrl("");
                            webview.addView(textView);
                        }
                    } else {
                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initWebView(final WebView webView) {

        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(true);//设置在WebView内部是否允许访问文件，默认允许访问。
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//设置WebView运行中的一个文件方案被允许访问其他文件方案中的内容，默认值true
        webView.getSettings().setAllowContentAccess(true);//设置WebView是否使用其内置的变焦机制，该机制结合屏幕缩放控件使用，默认是false，不使用内置变焦机制
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//设置WebView运行中的脚本可以是否访问任何原始起点内容，默认true
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);//设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webView.getSettings().setLoadsImagesAutomatically(true);//设置WebView是否加载图片资源，默认true，自动加载图片
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置脚本是否允许自动打开弹窗，默认false，不允许

//        webView.setVerticalScrollBarEnabled(false);// 取消Vertical ScrollBar显示
//        webView.setHorizontalScrollBarEnabled(false);// 取消Horizontal ScrollBar显示
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                String url = webResourceRequest.getUrl().toString();
                if (url.startsWith("http") || url.startsWith("https")) {
                    WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                    //hitTestResult==null解决重定向问题
                    if (!TextUtils.isEmpty(url) && hitTestResult == null) {
                        webView.loadUrl(url);
                        return true;
                    } else {
                        return super.shouldOverrideUrlLoading(webView, url);
                    }
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, "不支持的协议类型", Toast.LENGTH_SHORT).show();
                        return true;
                    }

                }
            }

        });

        webView.setOnKeyListener(new View.OnKeyListener() {//防止遇到重定向
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                        webView.goBack();
                        webView.removeAllViews();
                        return true;
                    }
                }
                return false;
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(s);
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
                webview.removeAllViews();
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
            webview.clearHistory();
            webview.clearView();
            webview.removeAllViews();
            webview.destroy();
            webview = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == QRSCAN_REQ_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                qrScan();
            } else {
                Toast.makeText(this, "扫码需要获取相机权限，请授予", Toast.LENGTH_SHORT).show();
            }
        }
    }

}



