package com.feige.myexplorer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Author: wutengfei
 * Date: 2023/1/5
 * Description:
 */
public class PicUtils {
    /**
     * 保存url中的图片到相册
     *
     * @param mContext
     * @param url
     */
    public static void url2bitmap(final Context mContext, String url) {
        Bitmap bm = null;
        try {
            URL iconUrl = new URL(url);
            URLConnection conn = iconUrl.openConnection();
            HttpURLConnection http = (HttpURLConnection) conn;
            int length = http.getContentLength();
            conn.connect();
            // 获得图像的字符流
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, length);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
            if (bm != null) {
                save2Album(mContext, bm);
            }
        } catch (final Exception e) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    /**
     * 保存到相册
     *
     * @param mContext
     * @param bitmap
     */
    private static void save2Album(final Context mContext, Bitmap bitmap) {
        File appDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM), "");
        if (!appDir.exists()) appDir.mkdirs();
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            insert2Gallery(mContext, fileName, file);
        } catch (final IOException e) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    /**
     * base64类型的url保存到相册
     *
     * @param mContext
     * @param base64DataStr
     */
    public static void savePicture(final Context mContext, String base64DataStr) {
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        // 1.去掉base64中的前缀
        String base64Str = base64DataStr.substring(base64DataStr.indexOf(",") + 1, base64DataStr.length());
        // 2.获取手机相册的路径地址
        File appDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM), "");
        if (!appDir.exists()) appDir.mkdirs();
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        File file = new File(appDir, fileName);
        // 3. 解析保存图片
        byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
        //创建一个将bytes作为其缓冲区的ByteArrayInputStream对象
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(data);
        //创建从底层输入流中读取数据的缓冲输入流对象
        bis = new BufferedInputStream(byteInputStream);
        // 创建到指定文件的输出流
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length != -1) {
                bos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            bos.flush();
            bos.close();
        } catch (final Exception e) {
            e.printStackTrace();
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        insert2Gallery(mContext, fileName, file);
    }

    /**
     * 插入到相册 并通知相册刷新
     *
     * @param mContext
     * @param fileName
     * @param file
     */
    private static void insert2Gallery(final Context mContext, String fileName, File file) {
        try {
            // 把文件插入到系统图库
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
