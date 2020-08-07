package com.dakticket.courier;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private WebView myWeb;
    SwipeRefreshLayout swipe;
    String urls="https://dakticket.com";
    Vector vists = new Vector();
    boolean isPageError = false;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;




    public Boolean checkReq(){
        List<String>permissionNeed= new ArrayList<>();
        for(String perm:PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this,perm)
                    !=PackageManager.PERMISSION_GRANTED){
                permissionNeed.add(perm);
            }
        }
        if(!permissionNeed.isEmpty()){
            ActivityCompat.requestPermissions(this,permissionNeed.toArray(new String[permissionNeed.size()]),
                    1234);
            return false;

        }
        return true;
    }



    String[] PERMISSIONS = {

            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1234){
            HashMap<String,Integer> permissionResults=new HashMap<>();
            int deniedCount=0;
            for (int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    permissionResults.put(permissions[i],grantResults[i]);
                    deniedCount++;
                }
            }
            if(deniedCount==0){
                Log.d("Permission","agreed");
                init();
            }
            else {
                Log.d("Permission","denied");

            }
        }

    }





    public void init(){
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        swipe.setProgressBackgroundColorSchemeColor(Color.WHITE);
        swipe.setColorSchemeColors(Color.rgb(255, 195, 11));
        webAction(urls);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                webAction(urls);
                //swipe.setRefreshing(false);
                // System.out.println(internets.isInternetOn(getApplicationContext()));
            }
        });


    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
       if(  checkReq())
           init();


    }


    public void webAction(String url){

        myWeb= (WebView) findViewById(R.id.web);
        myWeb.getSettings().setJavaScriptEnabled(true);
        myWeb.getSettings().setDomStorageEnabled(true);
        myWeb.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        myWeb.getSettings().getAllowFileAccess();
        myWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // myWeb.clearHistory();
        myWeb.clearCache(true);
        final ProgressDialog mbar = new ProgressDialog(this);
        mbar.setMessage("Loading");
        mbar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        myWeb.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
             //   if(urls.equals("https://dakticket.com/home/")) {
                    if (newProgress == 100) {
                        mbar.dismiss();
                    } else {
                        mbar.show();

                    }
               // }
                    super.onProgressChanged(view, newProgress);
            }
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });





        myWeb.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isPageError = false;
            }


            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                myWeb.loadUrl("file:///android_asset/error.html");
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                isPageError = true;
            }

            public void onPageFinished(WebView view, String url) {
                if (isPageError){
                    //myWeb.setVisibility(View.GONE);
                    myWeb.loadUrl("file:///android_asset/error.html");
                }
                swipe.setRefreshing(false);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(!url.equalsIgnoreCase("file:///android_asset/error.html")){
                    urls=url;
                }
                if(url.startsWith("http:")|| url.startsWith("https:")){
                    vists.add(url);
                    Log.d("show url",vists.lastElement().toString());
                    return false;
                }else if(url.startsWith("tel:")||url.startsWith("sms:")){

                    if(url.startsWith("tel:")){
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    else if(url.startsWith("sms:")){
                        sparse(url);
                        return true;
                    }
                }
                return false;
            }


        });
        myWeb.loadUrl(url);
    }

    public void sparse(String url){
        String phone,text;
        String s[]=url.split(":");
        String s1[]=s[1].split("\\?");
        phone=s1[0];
        String s2[]=s1[1].split("=");
        text=s2[1];
        sendText(phone,text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != 123 || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    void sendText(String phoneNo,String text){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNo));
        if(intent.hasExtra("sms_body")) {
            intent.replaceExtras(intent);
            intent.putExtra("sms_body", text);
        }
        else {
            intent.putExtra("sms_body", text);
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (myWeb.canGoBack()) {
            if(myWeb.canGoBack())
                myWeb.goBack();

        } else {
            super.onBackPressed();
        }
    }

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), 123);
    }
}

















