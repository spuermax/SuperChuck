package com.superdev.chuck;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;

public class MainActivity extends AppCompatActivity {

    private WebView tbs_web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tbs_web = findViewById(R.id.webView);
        //非wifi情况下，主动下载x5内核
        QbSdk.setDownloadWithoutWifi(true);
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            }

            @Override
            public void onCoreInitFinished() {

            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);


        tbs_web.getSettings().setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        tbs_web.getSettings().setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        tbs_web.getSettings().setDisplayZoomControls(true); //隐藏原生的缩放控件
        tbs_web.getSettings().setBlockNetworkImage(false);//解决图片不显示
        tbs_web.getSettings().setLoadsImagesAutomatically(true); //支持自动加载图片
        tbs_web.getSettings().setDefaultTextEncodingName("utf-8");//设置编码格式

        String url="http://39.106.37.143:9998/testCard/helpDetails.html?id=92";
        tbs_web.loadUrl(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onResume() {
        super.onResume();
        tbs_web.onResume();
        tbs_web.getSettings().setJavaScriptEnabled(true);
    }



}
