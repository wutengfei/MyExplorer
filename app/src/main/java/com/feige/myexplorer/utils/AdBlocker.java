package com.feige.myexplorer.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.WorkerThread;

import com.tencent.smtt.export.external.interfaces.WebResourceResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {

    private static final String AD_HOSTS_FILE = "hosts.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void initAdHost(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    loadFromAssets(context);
                } catch (IOException e) {
                    // noop
                }
                return null;
            }
        }.execute();
    }

    @WorkerThread
    private static void loadFromAssets(Context context) throws IOException {
        InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) AD_HOSTS.add(line);
        bufferedReader.close();
        inputStreamReader.close();
        stream.close();
    }

    public static boolean isAd(String url) {
        try {
            String host = new URL(url).getHost();
            if (TextUtils.isEmpty(host)) {
                return false;
            }
            int index = host.indexOf(".");
            return index >= 0 && (AD_HOSTS.contains(host));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }


}