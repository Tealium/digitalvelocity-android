package com.tealium.digitalvelocity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public final class WebViewActivity extends Activity {

    public static final String EXTRA_TITLE = "title";

    private ProgressBar activityIndicator;
    private ProgressBar pageProgress;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        setTitle(this.getIntent().getStringExtra(EXTRA_TITLE));

        this.activityIndicator = (ProgressBar) this.findViewById(R.id.webview_activityindicator);
        this.pageProgress = (ProgressBar) this.findViewById(R.id.webview_progressbar);

        WebView webView = (WebView) this.findViewById(R.id.webview_webview);
        webView.setWebViewClient(createWebViewClient());
        webView.setWebChromeClient(createWebChromeClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.loadUrl(this.getIntent().getDataString());
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                activityIndicator.setVisibility(View.GONE);
                pageProgress.setVisibility(View.GONE);
            }
        };
    }

    private WebChromeClient createWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                pageProgress.setProgress(newProgress);
            }
        };
    }
}
