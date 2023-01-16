package com.feige.myexplorer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.feige.myexplorer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: wutengfei
 * Date: 2023/1/16
 * Description:
 */
public class MyAdapter extends BaseAdapter {

    private List<String> list = new ArrayList<>();
    private Context context;
    private EditText et_url;
    private PopupWindow popupWindow;
    private WebView webView;

    public MyAdapter(Context context, List<String> data, EditText et_url, WebView webView, PopupWindow popupWindow) {
        this.context = context;
        list = data;
        this.et_url = et_url;
        this.popupWindow = popupWindow;
        this.webView = webView;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * 这里必须返回list.get(position)，否则点击条目后输入框显示的是position，而非该position的数据
     *
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        TvViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_search_listview, null);
            holder = new TvViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);

        } else {
            holder = (TvViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_url.setText(list.get(position));
                webView.loadUrl(list.get(position));
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_url.getWindowToken(), 0);
                if (popupWindow != null)
                    popupWindow.dismiss();
            }
        });
        //注意这里不要为convertView添加点击事件，默认是点击后：①下拉窗收起；
        //②点击的条目数据会显示在搜索框中；③光标定位到字符串末位。
        //如果自己添加点击事件，就要首先实现上面的①、②、③。

        holder.tv.setText(list.get(position));
        return convertView;
    }

    class TvViewHolder {
        TextView tv;
    }

}
