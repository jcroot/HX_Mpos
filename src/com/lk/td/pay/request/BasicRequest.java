package com.lk.td.pay.request;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.wedget.MyDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by wsq on 2016/5/20.
 */
public class BasicRequest{

    public static final int FLAG_REQUEST_SUCCESS = 0;       //数据请求成功
    public static final int FLAG_REQUEST_UNSUCCESS = 1;     //数据返回失败
    public static final int FLAG_REQUEST_FAIL = 2;          //请求失败

    public static final String  KEY_DATA = "DATA";
    public static final String KEY_URL = "URL";
    private static Message msg = new Message();
    private static MyDialog dialog;

    public static void sendRequest(Context mContext,String  mUrl,HashMap<String, String> mParams, Handler handler){
        MyHttpClient.post(mContext, mUrl, mParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Logger.json(new String(responseBody));
                try {
                    BasicResponse response = new BasicResponse(responseBody).getResult();
                    Bundle bundle = new Bundle();
                    if (response.isSuccess()){
                        msg.what = FLAG_REQUEST_SUCCESS;
                    }else{
                        msg.what = FLAG_REQUEST_UNSUCCESS;
                    }
                    bundle.putString(KEY_DATA, response.getJsonBody().toString());
                    bundle.putString(KEY_URL, mUrl);
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                msg.what = FLAG_REQUEST_FAIL;
                handler.sendMessage(msg);
            }

            @Override
            public void onStart() {
                super.onStart();
                if (dialog == null){
                    dialog = new MyDialog(mContext, R.style.FullScreenDialogAct);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setText("加载中...");
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();

                if (null != dialog){
                    dialog.dismiss();
                }
            }
        });

    }
}
