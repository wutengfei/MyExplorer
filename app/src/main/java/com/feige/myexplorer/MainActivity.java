package com.feige.myexplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.feige.myexplorer.adapter.MyAdapter;
import com.feige.myexplorer.utils.AdBlocker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.org.bjca.signet.component.qr.activity.SignetQrApi;
import cn.org.bjca.signet.component.qr.bean.QrResultBean;
import cn.org.bjca.signet.component.qr.callback.QrBaseCallBack;

import static com.feige.myexplorer.utils.AdBlocker.initAdHost;
import static com.feige.myexplorer.utils.OtherUtils.showAlert;
import static com.feige.myexplorer.utils.PicUtils.base64Url2bitmap;
import static com.feige.myexplorer.utils.PicUtils.url2bitmap;

/**
 * Author: wutengfei
 * Date: 2023/1/1
 * Description:  首页
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_url;
    private Button btn_enter;
    private WebView webview;
    private ImageView qr_scan;
    private boolean mIsExit;
    private FrameLayout mLayout;
    private LinearLayout ll_title;
    private ProgressBar progress;
    public static boolean isLandscape;
    private Context context;
    private static final int QRSCAN_REQ_CAMERA_PERMISSION = 776;
    private static final String TAG = "MainActivity";
    private boolean isSelectAll;
    private ArrayList<String> data;
    private String homePage = "file:///android_asset/active_pin.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.gray));
        initView();
        initListener();
        initData();
        initAdHost(context);
    }

    private void initView() {
        et_url = findViewById(R.id.et_url);
        btn_enter = findViewById(R.id.btn_enter);
        qr_scan = findViewById(R.id.qr_scan);
        webview = findViewById(R.id.webview);
        mLayout = findViewById(R.id.fl_video);
        ll_title = findViewById(R.id.ll_title);
        progress = findViewById(R.id.progress);
    }

    private void initListener() {
        btn_enter.setOnClickListener(this);
        qr_scan.setOnClickListener(this);
        //在该Editview获得焦点的时候将“回车”键改为“搜索”
        et_url.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        et_url.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        et_url.setSingleLine(true);  //不然回车【搜索】会换行
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

        et_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDropDown();
                if (!isSelectAll) {
                    et_url.selectAll();
                    isSelectAll = true;
                } else {
                    isSelectAll = false;
                }
            }
        });
    }

    private void initData() {
        initWebView(webview, progress);
//        webview.loadUrl(homePage);
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

    private void search(View view) {
        webview.removeAllViews();
        String url = et_url.getText().toString().trim();
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("https") || url.startsWith("http")) {
                webview.loadUrl(url);
            } else {
                String url_enter = "http://" + url;
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

    private void showDropDown() {
        View popView = getLayoutInflater().inflate(R.layout.listview, null);
        PopupWindow popupWindow = new PopupWindow(popView, et_url.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
        if (data == null || data.size() == 0) {
            data = new ArrayList<String>();
            data.add("http://k1k.cc/ju/list");
            data.add("http://www.bajjj.com");
            data.add("https://www.kuaiju5.com");
            data.add("https://www.jsdrtzn.com");
            data.add("http://www.imj6.com");
            data.add("https://meijui.cc");
            data.add("https://www.bbzmj.com");
            data.add("https://www.meijuw.com");
            data.add("https://m.meijutt.org");
        }
        MyAdapter adapter = new MyAdapter(context, data, et_url, webview, popupWindow);
        ListView listView = popView.findViewById(R.id.listview);
        listView.setAdapter(adapter);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setHeight(800);
        popupWindow.showAsDropDown(et_url);
    }

    private void initWebView(final WebView webView, ProgressBar progress) {
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setAllowFileAccess(true);//设置在WebView内部是否允许访问文件，默认允许访问。
        webView.getSettings().setAllowFileAccessFromFileURLs(true);//设置WebView运行中的一个文件方案被允许访问其他文件方案中的内容，默认值true
        webView.getSettings().setAllowContentAccess(true);//设置WebView是否使用其内置的变焦机制，该机制结合屏幕缩放控件使用，默认是false，不使用内置变焦机制
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//设置WebView运行中的脚本可以是否访问任何原始起点内容，默认true
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);//设置是否开启DOM存储API权限，默认false，未开启，设置为true，WebView能够使用DOM storage API
        webView.getSettings().setLoadsImagesAutomatically(true);//设置WebView是否加载图片资源，默认true，自动加载图片
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//设置脚本是否允许自动打开弹窗，默认false，不允许

//        webView.setVerticalScrollBarEnabled(false);// 取消Vertical ScrollBar显示
//        webView.setHorizontalScrollBarEnabled(false);// 取消Horizontal ScrollBar显示
        webView.setWebChromeClient(new MyWebChromeClient(context, mLayout, webview, ll_title, progress));
        webView.setWebViewClient(new WebViewClient() {
            private Map<String, Boolean> loadedUrls = new HashMap<>();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                Log.e(TAG, "onPageStarted: url-" + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                Log.e(TAG, "onPageFinished: url-" + url);
                if (!homePage.equals(url)) {
                    et_url.setText(url);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                String url = webResourceRequest.getUrl().toString();
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                String host = uri.getHost();

//                Log.e(TAG, "shouldOverrideUrlLoading: scheme-" + scheme);
//                Log.e(TAG, "shouldOverrideUrlLoading: url-" + url);
                if (url.startsWith("http") || url.startsWith("https")) {
                    return false;//返回false则webview继续加载url，返回true则停止加载url
                } else {

                    if (!TextUtils.isEmpty(scheme) && !TextUtils.isEmpty(host)) {
                        if (scheme != null && scheme.contains("baidu")) {
                            return true;
                        }
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "shouldOverrideUrlLoading:不支持的协议类型-" + url);
                        }
                    }
                    return true;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }

                if (ad) {
                    return AdBlocker.createEmptyResource();
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                //在加载资源时过滤广告标签
                //对网页加载速度稍有影响
//                hidenBanner(view);
//                hidenViewMore(view);
                super.onLoadResource(view, url);
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
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int type = result.getType();
                switch (type) {
                    case WebView.HitTestResult.EDIT_TEXT_TYPE: // 选中的文字类型
                        break;
                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // &emsp;地图类型
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 带有链接的图片类型
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                        savePicture2Gallery(result);
                        break;
                    case WebView.HitTestResult.UNKNOWN_TYPE: //未知
                        break;
                }
                return false;
            }
        });

    }

    private void hidenBanner(WebView view) {
        //JS，过滤带广告的Div标签(Daily)
        String javascript = "javascript:function hideBanner() {" +
                "var banners = document.getElementsByClassName('Daily');" +
                "var firstbanner = banners[0];" +
                "firstbanner.remove();" + "}";
        view.loadUrl(javascript);
        view.loadUrl("javascript:hideBanner();");
    }

    private void hidenViewMore(WebView view) {
        //JS，过滤带广告的标签(view-more)
        String javascript = "javascript:function hideViewMore() {" +
                "var ViewMore = document.getElementsByClassName('view-more');" +
                "var firstViewMore = ViewMore[0];" +
                "firstViewMore.remove();" + "}";
        view.loadUrl(javascript);
        view.loadUrl("javascript:hideViewMore();");
    }

    /**
     * 保存图片到相册
     *
     * @param result
     */
    private void savePicture2Gallery(final WebView.HitTestResult result) {
        showAlert(context, "提示", "是否保存图片", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = result.getExtra();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (URLUtil.isValidUrl(url)) {
                            url2bitmap(context, url);
                        } else {
                            if (url != null) {
                                base64Url2bitmap(context, url);
                            }
                        }
                    }
                }).start();
            }
        }, true);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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



