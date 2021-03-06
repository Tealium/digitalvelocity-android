package com.tealium.digitalvelocity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.TrackingManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public final class SnowshoeActivity extends DrawerLayoutActivity {

    private static final int PAGE_STATUS_UNLOADED = 0;
    private static final int PAGE_STATUS_LOADING = 1;
    private static final int PAGE_STATUS_LOADED = 2;
    private static final int PAGE_STATUS_ERROR = 3;

    private ViewState mViewState;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snowshoe);

        final String dbPath = this.getDatabasePath("snowshoe").getAbsolutePath();

        mViewState = new ViewState(this);
        mViewState.mWebView.setWebViewClient(createWebViewClient());

        final WebSettings settings = mViewState.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(dbPath);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            settings.setDatabasePath(dbPath);
        }

        if (Model.getInstance().getUserEmail() == null) {
            startActivity(new Intent(this, EmailActivity.class));
        } else {
            loadPage();
        }
    }

    @Override
    protected boolean isShowingLogo() {
        return true;
    }

    private void loadPage() {

        final Model model = Model.getInstance();
        final String userEmail = model.getUserEmail();

        final String snowshoeUrl = Model.getInstance().getSnowShoeUrl();
        final String url;


        if (!TextUtils.isEmpty(snowshoeUrl)) {
            url = snowshoeUrl;
        } else {
            url = new Uri.Builder()
                    .scheme("http")
                    .authority(Model.getInstance().getKeyManager().getSnowshoeAuthority())
                    .appendPath("index")
                    .appendQueryParameter("uid", userEmail)
                    .build().toString();
        }

        mViewState.mWebView.loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final int pageStatus = mViewState.mPageStatus;

        if ((pageStatus == PAGE_STATUS_UNLOADED || pageStatus == PAGE_STATUS_ERROR) &&
                Model.getInstance().getUserEmail() != null) {
            loadPage();
        }
    }

    @Override
    public void onBackPressed() {
        if (mViewState.mWebView.canGoBack()) {
            mViewState.mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    private void syncCookies(String url) {

        final Model model = Model.getInstance();
        final String userEmail = model.getUserEmail();
        final String visitorId = TrackingManager.getVisitorId();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        try {
            cookieManager.setCookie(url, "user_email=" + URLEncoder.encode(userEmail, "UTF-8"));


            if (visitorId != null) {
                cookieManager.setCookie(url, "visitor_id=" + URLEncoder.encode(visitorId, "UTF-8"));
            }

            final String name = (String) model.getVipData().get("vip_name");
            if (!TextUtils.isEmpty(name)) {
                cookieManager.setCookie(url, "user_name=" + URLEncoder.encode(name, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            cookieManager.flush();
        }

        final String pageTitleKey = "page_title=";
        final String cookie = cookieManager.getCookie(url);
        final int pageTitleIndex = cookie.indexOf(pageTitleKey);
        if (pageTitleIndex == -1) {
            return;
        }

        String pageTitle = cookie.substring(pageTitleIndex + pageTitleKey.length());
        final int semicolonIndex = pageTitle.indexOf(';');
        if (semicolonIndex != -1) {
            pageTitle = pageTitle.substring(0, semicolonIndex);
        }

        setTitle(pageTitle);
    }

    private WebViewClient createWebViewClient() {

        return new WebViewClient() {

            private boolean mErrorOccurred;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mViewState.setPageStatus(PAGE_STATUS_LOADING);
                mErrorOccurred = false;
            }

            @Override
            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                mErrorOccurred = true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mErrorOccurred = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (mErrorOccurred) {
                    mViewState.setPageStatus(PAGE_STATUS_ERROR);
                } else {
                    mViewState.setPageStatus(PAGE_STATUS_LOADED);
                }

                syncCookies(url);
            }
        };
    }

    private static final class ViewState {
        private final WebView mWebView;
        private final View mProgressBar;
        private final View mLoadingLabel;
        private final View mErrorLabel;
        private int mPageStatus;

        public ViewState(Activity activity) {
            mPageStatus = PAGE_STATUS_UNLOADED;
            mProgressBar = activity.findViewById(R.id.snowshoe_progressbar);
            mLoadingLabel = activity.findViewById(R.id.snowshoe_label_loading);
            mErrorLabel = activity.findViewById(R.id.snowshoe_label_error);
            mWebView = (WebView) activity.findViewById(R.id.snowshoe_webview);
        }

        public void setPageStatus(int pageStatus) {
            switch (pageStatus) {
                case PAGE_STATUS_UNLOADED:
                    mWebView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLoadingLabel.setVisibility(View.VISIBLE);
                    mErrorLabel.setVisibility(View.GONE);
                    break;
                case PAGE_STATUS_LOADING:
                    mWebView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLoadingLabel.setVisibility(View.VISIBLE);
                    mErrorLabel.setVisibility(View.GONE);
                    break;
                case PAGE_STATUS_LOADED:
                    mWebView.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    mLoadingLabel.setVisibility(View.GONE);
                    mErrorLabel.setVisibility(View.GONE);
                    break;
                case PAGE_STATUS_ERROR:
                    mWebView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mLoadingLabel.setVisibility(View.GONE);
                    mErrorLabel.setVisibility(View.VISIBLE);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            mPageStatus = pageStatus;
        }
    }
}
