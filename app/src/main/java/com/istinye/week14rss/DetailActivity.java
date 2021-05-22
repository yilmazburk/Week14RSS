package com.istinye.week14rss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DetailActivity extends Activity {

    private WebView detailWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        detailWebView = findViewById(R.id.detailWebView);
        detailWebView.getSettings().setJavaScriptEnabled(true);
        detailWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });


        Intent detailIntent = getIntent();
        String url = detailIntent.getStringExtra("URL");
        if (!url.isEmpty()) {
            detailWebView.loadUrl(url);
        }

    }
}